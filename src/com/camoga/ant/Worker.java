package com.camoga.ant;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import com.camoga.ant.ants.AbstractAnt;
import com.camoga.ant.ants.Ant;
import com.camoga.ant.ants.Ant3D;
import com.camoga.ant.ants.HexAnt;
import com.camoga.ant.level.Level;
import com.camoga.ant.net.Client;

public class Worker {

	long iterations = 0;
	
	Thread thread;	
	boolean kill;
	boolean running;
	int workerid;
	
	long autosavetimer;
	AbstractAnt ant;
	Level level;
	int type;
	
	public Worker(int ID, int type) {
		this.workerid = ID;
		this.type = type;
		if(type==0) {
			ant = new Ant(this);
			level = new Level(this,2);
		} else if(type==1) {
			ant = new HexAnt(this);
			level = new Level(this,2);
		} else if(type==2) {
			ant = new Ant3D(this);
			level = new Level(this,3);
		} else throw new RuntimeException();
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
			long iterations = p[1];

			ant.init(rule, iterations);
			this.iterations = 0;
			
			if(type==2) {
				int[] letters = Arrays.copyOf(ant.getRule().letter, ant.getRule().letter.length);
				if((letters[ant.getRule().getSize()-1]&1) == 0) continue;
				Arrays.sort(letters);
				int count = 1;
				int current = letters[0];
				for(int i = 0; i < letters.length; i++) {
					if(letters[i] != current) count++;
					current = letters[i];
				}
				if(count < 3) continue;				
			}
			
			time = System.nanoTime();
			long[] result = runRule(rule,iterations);
			Client.storeRules(type,result);
			float seconds = (float) ((-time + (time = System.nanoTime()))/1e9);
			Client.LOG.info(String.format("%s\t%s\t%s it/s\t%s s\t%s", Long.toUnsignedString(rule), ant.getRule().string(), this.iterations/seconds, seconds, (result[1] > 1 ? (result[1] + " " + Arrays.toString(Arrays.copyOfRange(result, 3, result.length))):result[1]==1 ? "?":"")));
		}
		Client.LOG.warning("Worker " + workerid + " has stopped");
		running = false;
		if(kill) WorkerManager.remove(this);
	}
	
	public long[] runRule(long rule, long maxiterations) {
		long max = maxiterations;
		boolean extended = false;
		while(!ant.periodFound() && (maxiterations == -1 || iterations < max)) {
			iterations += ant.move();
			if(level.deleteOldChunks) {
				getLevel().chunks.entrySet().removeIf(e -> iterations - e.getValue().lastVisit > 100000000);
			}
//			System.out.println(getLevel().chunks.size()/Math.pow(getLevel().maxChunk, 2) + ", " + iterations);
//			if(Settings.autosave && System.currentTimeMillis()-autosavetimer > 900000) { // Autosave every 15 mins
//				ant.saveState(ant.getRule()+".state");
//				System.out.println("Autosave");
//				autosavetimer = System.currentTimeMillis();
//			}
			
			//Detect cyclic "highways"
			if(type > 0 && !extended && getLevel().chunks.size() < 16 &&  maxiterations != -1 && iterations > maxiterations) {
				extended = true;
				max += 100000000;
				getAnt().setFindingPeriod(true);
			}
		}
		if(type == 0) {
			long period = ant.periodFound() ? ant.getPeriod():(ant.findingPeriod() ? 1:0);
			if(period <= 1) return new long[] {rule,period,iterations,0,0};
			long[] d = {Math.abs(ant.xend-ant.xstart), Math.abs(ant.yend-ant.ystart)};
			Arrays.sort(d);
			return new long[] {rule,period,iterations,d[1],d[0]};			
		} else if(type == 1) {
			if(ant.findingPeriod()) return new long[] {rule,0,iterations,1,0};
			if(!ant.periodFound()) return new long[] {rule,0,iterations,0,0};
			// Find the shortest path to dx,dy. We covert to cube coordinates and pick the ones with the same sign
			long[] coordinates = {ant.xend-ant.xstart,ant.yend-ant.ystart,0}; coordinates[2] = -coordinates[1] - coordinates[0];
			Arrays.sort(coordinates);
			if(coordinates[1] >= 0) return new long[] {rule,ant.getPeriod(),iterations,coordinates[2],coordinates[1]};
			return new long[] {rule,ant.getPeriod(),iterations,-coordinates[0],-coordinates[1]};
		} else if(type == 2) {
			long period = ant.periodFound() ? ant.getPeriod():(ant.findingPeriod() ? 1:0);
			if(period <= 1) return new long[] {rule,period,iterations,0,0,0};
			long[] d = {Math.abs(ant.xend-ant.xstart), Math.abs(ant.yend-ant.ystart), Math.abs(ant.zend-ant.zstart)};
			Arrays.sort(d);
			return new long[] {rule,period,iterations,d[2],d[1],d[0]};	
		}
		return null;
	}
	
	protected void saveImage(File file, boolean info) {
		BufferedImage image = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);
		level.render(((DataBufferInt)(image.getRaster().getDataBuffer())).getData(), image.getWidth(), image.getHeight(), true);
		if(info) {
			Graphics g = image.createGraphics();
			//TODO merge with render method
			g.setColor(Color.WHITE);
			g.drawString("Iterations: " + iterations, 10, 30); 
			g.drawString("Rule: " + ant.getRule().string() + " ("+ant.getRule().getRule()+")", 10, 46);
			if(ant.findingPeriod()) {
				g.setColor(Color.red);
				g.drawString("Finding period... " + ant.getPeriod(), 10, 62);
			} else if(ant.periodFound()) {
				g.setColor(Color.WHITE);
				g.drawString("Period: " + ant.getPeriod(), 10, 62);
			}			
		}
		
		try {
			ImageIO.write(image, "PNG", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
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