package com.camoga.ant;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import javax.imageio.ImageIO;

import com.camoga.ant.Level.Chunk;

public class Simulation {

	static long rule = 1;
//	static Ant ant;
	static long iterations = 0;
	
	static IRule nextrule = c -> c+1;
	static String log = "";
	static ArrayList<Long> savedRules;
	
	static Thread thread;
	static boolean running;
	static boolean finished = true;
	
	public Simulation(long startingrule, IRule nextrule) {
		Simulation.rule = startingrule;
		Simulation.nextrule = nextrule;
		if(Settings.ignoreSavedRules) savedRules = IORules.searchSavedRules(false);
		timer = System.currentTimeMillis();
		nextRule();
		start();
	}
	
	public Simulation(String file, IRule nextrule) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
			iterations = ois.readLong();
			rule = ois.readLong();
			System.out.println(rule);
			Rule.createRule(rule);
			Simulation.nextrule = nextrule;
			Ant.init();
			Ant.dir = ois.readInt();
			Ant.state = ois.readInt();
			Ant.x = ois.readInt();
			Ant.y = ois.readInt();
			Ant.xc = ois.readInt();
			Ant.yc = ois.readInt();
			Ant.saveState = ois.readBoolean();
			if(Ant.saveState) {
				Ant.index = ois.readLong();
				Ant.repeatLength = ois.readInt();
				Ant.minHighwayPeriod = ois.readLong();
				Ant.states = ois.readNBytes(200000000);
			}
			Level.chunks = (ArrayList<Level.Chunk>)ois.readObject();
			Level.lastChunk = Level.chunks.get(0);
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(Settings.ignoreSavedRules) savedRules = IORules.searchSavedRules(false);
		timer = System.currentTimeMillis();
		start();
	}
	
	public static boolean start() {
		if(!finished) return false;
		LangtonMain.tray.getTrayIcons()[0].setToolTip("Langton's Ant " + rule);
		thread = new Thread(() -> run(),"Simulation");
		thread.start();
		running = true;
		finished = false;
		return true;
	}
	
	public static void stop() {
		running = false;
	}
	
	public static void run() {
		while(running) {			
			iterations += Ant.move();
			
			if(Settings.deleteOldChunks) {
				Level.chunks.removeIf((Chunk c) -> iterations - c.lastVisit >= 100000000);
			}
			
			if(iterations > Settings.maxiterations && Settings.maxiterations != -1 || Ant.CYCLEFOUND) {
				saveRule();
				if(Settings.printlog)System.out.println(log);
				rule = nextrule.nextRule(rule);
				nextRule();
			}
		}
		finished = true;
	}
	
	protected static void saveBinHighway(File file) {
		byte[] pixels = new byte[Settings.highwaySizew*Settings.highwaySizeh]; //TODO Use mappedbytebuffer for >= 2GB files
		Level.renderHighway(pixels, Settings.canvasSize, Settings.highwaySizew, Settings.highwaySizeh, Settings.followAnt);
		
		try {
			FileOutputStream baos = new FileOutputStream(file);
			baos.write(ByteBuffer.allocate(4).putInt(Settings.highwaySizew).array());
			baos.write(pixels);
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
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
		if(Ant.saveState) {
			g.setColor(Color.red);
			g.drawString("Finding period... " + Ant.minHighwayPeriod, 10, 62);
		} else if(Ant.CYCLEFOUND) {
			g.setColor(Color.WHITE);
			g.drawString("Period: " + Ant.minHighwayPeriod, 10, 62);
		}
		
		try {
			ImageIO.write(image, "PNG", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveRule() {
		try {
			if(Ant.CYCLEFOUND) {
				if(Settings.toot && Ant.minHighwayPeriod > 10000) {
					//MASTODON BOT mastodon.social/@langtonant
					BufferedReader fr = new BufferedReader(new FileReader("utctimeschedule.txt"));
					long utctime = Long.parseLong(fr.readLine());
					long now = Instant.now().getEpochSecond();
					while(now > utctime+400) {
						utctime += 3600;
					}
					File tmpimg = File.createTempFile("langtonimg", ".png");
					saveImage(tmpimg);
					System.out.println("Toot at " + new SimpleDateFormat().format(new Date(utctime*1000)));
					Runtime.getRuntime().exec("python -c \"from mastodon import Mastodon; from datetime import datetime; m = Mastodon(access_token = '"+System.getenv("MASTODONTOKEN")+"', api_base_url = 'https://mastodon.social/'); m.status_post('Rule: "+Rule.string()+"\\nPeriod: "+Ant.minHighwayPeriod+"', media_ids=[m.media_post(r'"+tmpimg.getPath()+"')], scheduled_at=datetime.utcfromtimestamp("+utctime+"))\"");
					fr.close();
					FileWriter fw = new FileWriter("utctimeschedule.txt", false);
					fw.write(utctime+3600+"");
					fw.close();
				}
				if(Settings.savepic) {				
					File dir = new File(Ant.minHighwayPeriod+"");
					boolean newdir = !dir.exists() ? dir.mkdir():false;
					log += rule + "\t" + Ant.minHighwayPeriod + "\t" + (newdir ? " N":"")+"\n";
					saveImage(new File(Ant.minHighwayPeriod + "/"+rule+".png"));
				} else log += rule + "\t" + Ant.minHighwayPeriod + "\n";
			} else if(Ant.saveState) {
				log += rule + "\t" + "? " + Ant.minHighwayPeriod +"\n";
				if(Settings.savepic) saveImage(new File(0 + "/" + rule+".png"));
			}
			if(!Settings.saverule) return;
			FileOutputStream fos = new FileOutputStream(Settings.file, true);
			fos.write(ByteBuffer.allocate(16).putLong(rule).putLong(Ant.CYCLEFOUND ? Ant.minHighwayPeriod:(Ant.saveState ? 1:0)).array());
			fos.close();
			//TODO add tested rules to savedrules
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void init() {
		Level.init();
		Rule.createRule(rule);
		Ant.init();
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
