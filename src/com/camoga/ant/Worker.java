package com.camoga.ant;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import javax.imageio.ImageIO;

import com.camoga.ant.Level.Chunk;
import com.camoga.ant.net.Client;

public class Worker {

	long iterations = 0;
	
	Thread thread;	
	boolean running;
	int workerid;
	
	long autosavetimer;
	Ant ant;
	Level level;
	Rule rule;
	
	public Worker(int ID) {
		this.workerid = ID;
		ant = new Ant(this);
		level = new Level(this);
		rule = new Rule();
		start();
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
		while((p = Client.getRule())[0] != -1) {
			long rule = p[0];
			long iterations = p[1];

			level.init();
			ant.init(iterations);
			this.rule.createRule(rule);
			this.iterations = 0;
			
			time = System.currentTimeMillis();
			long[] result = runRule(rule,iterations);
			Client.storeRule(result);
			
			float seconds = (-time + (time = System.currentTimeMillis()))/1000f;
			Client.LOG.info(rule + "\t" + Rule.string(rule) + "\t " + this.iterations/seconds + " it/s\t" + seconds+ "s" + (result[1] > 1 ? "\t"+result[1]:"") + "\t nc: " + level.chunks.size());
		}
		Client.LOG.warning("Worker " + workerid + " has stopped");
		running = false;
	}
	
	public long[] runRule(long rule, long maxiterations) {
		while(!ant.CYCLEFOUND && (maxiterations == -1 || iterations < maxiterations)) {
			iterations += ant.move();
			if(Settings.deleteOldChunks) { //Delete old chunks
				// TODO write to highway file before deleting
//				Level.chunks.removeIf((Chunk c) -> iterations - c.lastVisit >= 100000000);
			}
			
			if(Settings.autosave && System.currentTimeMillis()-autosavetimer > 900000) { // Autosave every 15 mins
				LangtonMain.saveState();
				System.out.println("Autosave");
				autosavetimer = System.currentTimeMillis();
			}
		}
		long period = ant.CYCLEFOUND ? ant.minHighwayPeriod:(ant.saveState ? 1:0);
		
		return new long[] {rule,period,iterations};
//		saveRule();
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
	
	protected void saveImage(long rule, File file) {
//		Simulation.saveBinHighway(new File(Simulation.rule+".bin"));
//		if(0==0) return;
		BufferedImage image = new BufferedImage(Settings.saveImageW, Settings.saveImageH, BufferedImage.TYPE_INT_RGB);
		level.render(((DataBufferInt)(image.getRaster().getDataBuffer())).getData(), Settings.canvasSize, image.getWidth(), image.getHeight(), Settings.followAnt);
		Graphics g = image.createGraphics();
		//TODO merge with render method
		g.setColor(Color.WHITE);
		g.drawString("Iterations: " + iterations, 10, 30); 
		g.drawString("Rule: " + this.rule.string() + " ("+rule+")", 10, 46);
		if(ant.saveState) {
			g.setColor(Color.red);
			g.drawString("Finding period... " + ant.minHighwayPeriod, 10, 62);
		} else if(ant.CYCLEFOUND) {
			g.setColor(Color.WHITE);
			g.drawString("Period: " + ant.minHighwayPeriod, 10, 62);
		}
		
		try {
			ImageIO.write(image, "PNG", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Rule getRule() {
		return rule;
	}
	
	public long getIterations() {
		return iterations;
	}
	
	public Ant getAnt() {
		return ant;
	}
	
	public Level getLevel() {
		return level;
	}

	public boolean isRunning() {
		return running;
	}
}