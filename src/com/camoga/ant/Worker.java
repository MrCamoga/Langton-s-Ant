package com.camoga.ant;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import com.camoga.ant.ants.AbstractAnt;
import com.camoga.ant.ants.Ant;
import com.camoga.ant.ants.Ant3D;
import com.camoga.ant.ants.Ant4D;
import com.camoga.ant.ants.AntHex;
import com.camoga.ant.level.Level;
import com.camoga.ant.net.Client;

public class Worker {

	public long iterations = 0;
	
	Thread thread;	
	boolean kill;
	boolean running;
	int workerid;
	
//	boolean recordingVideo = false;
//	VideoCreator video;
	
	long autosavetimer;
	AbstractAnt ant;
	Level level;
	int type;
	
	public Worker(int ID, int type) {
		this.workerid = ID;
		this.type = type;
		level = new Level(this);
		if(type==0) {
			ant = new Ant(this);
		} else if(type==1) {
			ant = new AntHex(this);
		} else if(type==2) {
			ant = new Ant3D(this);
		}  else if(type==3) {
			ant = new Ant4D(this);
		} else throw new RuntimeException("Invalid type of ant");
	}
	
	public void start() {
		if(running) return;
		thread = new Thread(() -> run(), "AntWorker"+workerid);
		thread.start();
		Client.LOG.info("Worker " + workerid + " started");
		running = true;
	}
	
	public void run() {
		long[] p;
		long time;
		while((p = Client.getRule(type)) != null && !kill) {
			long rule = p[0];
			long maxiterations = p[1];
			
			time = System.nanoTime();
			long[] result = runRule(rule,maxiterations);
			Client.storeRules(type,result);
			float seconds = (float) ((-time + (time = System.nanoTime()))/1e9);
			Client.LOG.info(String.format("%02d %s\t%s\t%.4E it/s\t%.4f s\t%s", workerid, Long.toUnsignedString(rule), ant.getRule(), this.iterations/seconds, seconds, (result[1] > 1 ? (result[1] + " " + Arrays.toString(Arrays.copyOfRange(result, 3, result.length))):result[1]==1 ? "?":"")));
		}
		Client.LOG.warning("Worker " + workerid + " has stopped");
		running = false;
		if(kill) WorkerManager.remove(this);
	}
	
	public long[] runRule(long rule, long maxiterations) {
		long max = maxiterations;
		boolean extended = false;
		ant.init(rule, maxiterations);
		this.iterations = 0;

		int maxChunk = 1;
		
		while(!ant.periodFound() && (maxiterations == -1 || iterations < max)) {
			iterations += ant.move();

			// Chunk deletion only activates when highway has started
			if(level.deleteOldChunks && iterations > 1000000000) {
				getLevel().chunks.entrySet().removeIf(e -> iterations - e.getValue().lastVisit > 1000000000);
			}
			
			// Detect highways
			if(!getAnt().findingPeriod() && !getAnt().periodFound()) {
				//Farthest chunk ant has traveled
				int maxc = Math.max(Math.abs(getAnt().getXC()), Math.abs(getAnt().getYC()));
				if(maxc > maxChunk) maxChunk = maxc;
				if(maxChunk > Settings.chunkCheck && level.chunks.size() < 0.2*maxChunk*maxChunk) { // Proportion of chunks generated over size of square that bounds all chunks. If prop -> 0 ant forms a highway (prop might go near 0 if ant forms a thin triangle)
					getLevel().deleteOldChunks = true;
					setFindingPeriod(true);
				}
			}
			
			if(Settings.autosave && maxiterations > 50e9 && System.currentTimeMillis()-autosavetimer > 900000) { // Autosave every 15 mins
				ant.saveState(ant.getRule()+".state");
				Client.LOG.info("Autosaving " + getAnt().getRule());
				autosavetimer = System.currentTimeMillis();
			}
			
			if((type > 0 || (type==0 && (rule & (rule+1))==0)) && !extended && getLevel().chunks.size() <= (1<<ant.dimension) &&  maxiterations != -1 && iterations > maxiterations) {
				extended = true;
				max += 100000000;
				getAnt().setFindingPeriod(true);
			}
		}
		if(type == 0) { // 2d ant square grid
			long period = ant.periodFound() ? ant.getPeriod():(ant.findingPeriod() ? 1:0);
			long winding = (ant.directionend-ant.directionstart);
			long[] d = {Math.abs(ant.xend-ant.xstart), Math.abs(ant.yend-ant.ystart)};
			if(period > 1) { // detect triangles
				double dist = Math.sqrt(ant.getX()*ant.getX()+ant.getY()*ant.getY());
				double highdist = Math.sqrt(d[0]*d[0]+d[1]*d[1]);
				double dot = (ant.getX()*(ant.xend-ant.xstart)+ant.getY()*(ant.yend-ant.ystart))/(dist*highdist);
				if(dot < 0.3) period = 0;
				if((winding&3) != 0) period = 1;
			}
			if(period <= 1) return new long[] {rule,period,iterations,0,0,0};
			Arrays.sort(d);
			return new long[] {rule,period,iterations,d[1],d[0],winding>>2};
		} else if(type == 1) { // hex ant
			if(ant.findingPeriod()) return new long[] {rule,0,iterations,1,0};
			if(!ant.periodFound()) return new long[] {rule,0,iterations,0,0};
			// Find the shortest path to dx,dy. We convert to cube coordinates and pick the ones with the same sign
			long[] coordinates = {ant.xend-ant.xstart,ant.yend-ant.ystart,0}; coordinates[2] = -coordinates[1] - coordinates[0];
			Arrays.sort(coordinates);
			if(coordinates[1] >= 0) return new long[] {rule,ant.getPeriod(),iterations,coordinates[2],coordinates[1]};
			return new long[] {rule,ant.getPeriod(),iterations,-coordinates[0],-coordinates[1]};
		} else if(type == 2) { // 3d ant
			long period = ant.periodFound() ? ant.getPeriod():(ant.findingPeriod() ? 1:0);
			if(period <= 1) return new long[] {rule,period,iterations,0,0,0};
			long[] d = {Math.abs(ant.xend-ant.xstart), Math.abs(ant.yend-ant.ystart), Math.abs(ant.zend-ant.zstart)};
			Arrays.sort(d);
			return new long[] {rule,period,iterations,d[2],d[1],d[0]};	
		} else if(type == 3) { // 4d ant
			long period = ant.periodFound() ? ant.getPeriod():(ant.findingPeriod() ? 1:0);
			if(period <= 1) return new long[] {rule,period,iterations,0,0,0,0};
			long[] d = {Math.abs(ant.xend-ant.xstart), Math.abs(ant.yend-ant.ystart), Math.abs(ant.zend-ant.zstart), Math.abs(ant.wend-ant.wstart)};
			Arrays.sort(d);
			return new long[] {rule,period,iterations,d[3],d[2],d[1],d[0]};	
		}
		return null;
	}
	
	protected void saveImage(File file, boolean info) {
		BufferedImage image = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);
		level.render(image, ((DataBufferInt)(image.getRaster().getDataBuffer())).getData(), image.getWidth(), image.getHeight(), true, info);
		
		try {
			ImageIO.write(image, "PNG", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setFindingPeriod(boolean b) {
		getAnt().setFindingPeriod(b);
	}
	
	public long getIterations() {
		return iterations;
	}
	
	public AbstractAnt getAnt() {
		return ant;
	}
	
	public Level getLevel() {
		return level;
	}

	public boolean isRunning() {
		return running;
	}
	
	public int getType() {
		return type;
	}

	public void kill() {
		kill = true;
	}
}