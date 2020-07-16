package com.camoga.ant;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import com.camoga.ant.ants.AbstractAnt;
import com.camoga.ant.ants.Ant;
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
		if(type==0) {
			ant = new Ant(this);
			this.type = 0;
		} else if(type==1) {
			ant = new HexAnt(this);
			this.type = 1;
		} else throw new RuntimeException();
		level = new Level(this);
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
		long time = System.currentTimeMillis();
		while((p = Client.getRule(type)) != null && !kill) {
			long rule = p[0];
			long iterations = p[1];

			level.init();
			ant.init(rule, iterations);
			this.iterations = 0;
			
			time = System.currentTimeMillis();
			long[] result = runRule(rule,iterations);
			Client.storeRules(type,result);
			
			float seconds = (-time + (time = System.currentTimeMillis()))/1000f;
			if(type == 0) Client.LOG.info(Long.toUnsignedString(rule) + "\t" + ant.getRule().string() + "\t " + this.iterations/seconds + " it/s\t" + seconds+ "s\t" + (result[1] > 1 ? result[1]:result[1]==1 ? "?":""));
			else if(type == 1) Client.LOG.info(Long.toUnsignedString(rule) + "\t" + ant.getRule().string() + "\t " + this.iterations/seconds + " it/s\t" + seconds+ "s\t" + (result[1] > 0 ? result[1]:result[1]==-1 ? "?":""));
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
//			if(Settings.deleteOldChunks) {
//				 TODO write to highway file before deleting
//				getLevel().chunks.entrySet().removeIf(e -> iterations - e.getValue().lastVisit >= 1000000000);

//			}
			
//			if(Settings.autosave && System.currentTimeMillis()-autosavetimer > 900000) { // Autosave every 15 mins
//				ant.saveState(ant.getRule()+".state");
//				System.out.println("Autosave");
//				autosavetimer = System.currentTimeMillis();
//			}
			
			if(type == 1 && !extended && getLevel().chunks.size() < 8 && iterations > maxiterations) {
				extended = true;
				max += 100000000;
				getAnt().setFindingPeriod(true);
			}
		}
		
		long period = ant.periodFound() ? ant.getPeriod():(ant.findingPeriod() ? (type == 0 ? 1:-1):0);
		
		return new long[] {rule,period,iterations};
	}
	
	protected void saveBinHighway(File file) {
		byte[] pixels = new byte[Settings.highwaySizew*Settings.highwaySizeh]; //TODO Use mappedbytebuffer for >= 2GB files  or  calculate the highway size on the fly
		
		try {
//			MyMappedByteBuffer mbb = new MyMappedByteBuffer(file);
//			mbb.put(0, ByteBuffer.allocate(8).putInt(Settings.highwaySizew).putInt(Settings.highwaySizeh).array());
//			System.out.println();
//			Level.renderHighway(mbb, Settings.canvasSize, Settings.highwaySizew, Settings.highwaySizeh, Settings.followAnt);
			level.renderHighway(pixels, Settings.canvasSize, Settings.highwaySizew, Settings.highwaySizeh, Settings.followAnt);
			FileOutputStream baos = new FileOutputStream(file);
			baos.write(ByteBuffer.allocate(8).putInt(Settings.highwaySizew).putInt(Settings.highwaySizeh).array());
			baos.write(pixels);
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void saveImage(File file, boolean info) {
//		Simulation.saveBinHighway(new File(Simulation.rule+".bin"));
//		if(0==0) return;
		BufferedImage image = new BufferedImage(Settings.saveImageW, Settings.saveImageH, BufferedImage.TYPE_INT_RGB);
		level.render(((DataBufferInt)(image.getRaster().getDataBuffer())).getData(), Settings.canvasSize, image.getWidth(), image.getHeight(), true);
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