package com.camoga.ant;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;

import com.camoga.ant.Level.Chunk;

public class Simulation {


	static long rule = 1;
	static Ant ant;
	static long iterations = 0;
	
	static IRule nextrule = c -> c+1;
	static String log = "";
	static ArrayList<Long> savedRules;
	
	static Thread thread;
	static boolean running;
	
	public Simulation(long startingrule, IRule nextrule) {
		Simulation.rule = startingrule;
		Simulation.nextrule = nextrule;
		if(Settings.ignoreSavedRules) savedRules = IORules.searchSavedRules(false);
		timer = System.currentTimeMillis();
		nextRule();
		start();
	}
	
	public static void start() {
		thread = new Thread(() -> run(),"Simulation");
		thread.start();
		running = true;
	}
	
	public static void stop() {
		running = false;
	}
	
	public static void run() {
		while(running) {			
			iterations += ant.move();
			
			if(Settings.deleteOldChunks) {
				Level.chunks.removeIf((Chunk c) -> iterations - c.lastVisit >= 100000000);
			}
			
			if(iterations > Settings.maxiterations || ant.CYCLEFOUND) {
				saveRule();
				if(Settings.printlog)System.out.println(log);
				rule = nextrule.nextRule(rule);
				nextRule();
			}
		}
	}
	
	protected static void saveImage(File file) {
		BufferedImage image = new BufferedImage(Settings.saveImageW, Settings.saveImageH, BufferedImage.TYPE_INT_RGB);
		Level.render(((DataBufferInt)(image.getRaster().getDataBuffer())).getData(), Settings.canvasSize, image.getWidth(), image.getHeight(), Settings.followAnt);
		Graphics g = image.createGraphics();
		//TODO merge with render method
		g.setColor(Color.WHITE);
		g.drawString("Iterations: " + iterations, 10, 30); 
		g.drawString("Rule: " + Rule.string() + " ("+rule+")", 10, 46);
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
	
	public static void saveRule() {
		try {
			if(ant.CYCLEFOUND) {
				if(Settings.toot && ant.minHighwayPeriod > 5000) {
					//MASTODON BOT mastodon.social/@langtonant
				}
				if(Settings.savepic) {				
					File dir = new File(ant.minHighwayPeriod+"");
					boolean newdir = !dir.exists() ? dir.mkdir():false;
					log += rule + "\t" + ant.minHighwayPeriod + "\t" + (newdir ? " N":"")+"\n";
					saveImage(new File(ant.minHighwayPeriod + "/"+rule+".png"));
				} else log += rule + "\t" + ant.minHighwayPeriod + "\n";
			} else if(ant.saveState) {
				log += rule + "\t" + "? " + ant.minHighwayPeriod +"\n";
				if(Settings.savepic) saveImage(new File(0 + "/" + rule+".png"));
			}
			if(!Settings.saverule) return;
			FileOutputStream fos = new FileOutputStream(Settings.file, true);
			fos.write(ByteBuffer.allocate(16).putLong(rule).putLong(ant.CYCLEFOUND ? ant.minHighwayPeriod:(ant.saveState ? 1:0)).array());
			fos.close();
			//TODO add tested rules to savedrules
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void init() {
		Level.init();
		Rule.createRule(rule);
		ant = new Ant(0,0);
		iterations = 0;
//		System.gc();
	}
	
	static long timer;
	
	public static void nextRule() {
		
		if(savedRules!=null) while(Collections.binarySearch(savedRules, rule) >= 0) {
			rule = nextrule.nextRule(rule);
		}
		if(Settings.printlog)System.out.println(rule + "   " + Rule.string(rule));
		init();
		System.out.println((System.currentTimeMillis()-timer)/1000.0 + "s");
		timer = System.currentTimeMillis();
	}
}
