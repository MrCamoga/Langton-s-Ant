package com.camoga.ant;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedReader;
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
import java.util.Date;

import javax.imageio.ImageIO;

import com.camoga.ant.Level.Chunk;

public class Simulation {

	public static long iterations = 0;
	
	static Thread thread;
	static boolean running;
	static long autosavetimer;
	
	public static void init(String file, IRule nextrule) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
			iterations = ois.readLong();
			long rule = ois.readLong();
			System.out.println(rule);
			Rule.createRule(rule);
//			Simulation.nextrule = nextrule;
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

//		if(Settings.ignoreSavedRules) savedRules = IORules.searchSavedRules(false);
		autosavetimer = System.currentTimeMillis();
	}
	
	
	public static void stop() {
		running = false;
	}
	
	public static long[] runRule(long rule) {
		while(!Ant.CYCLEFOUND && (Settings.maxiterations == -1 || iterations < Settings.maxiterations)) {
			iterations += Ant.move();
			if(Settings.deleteOldChunks) { //Delete old chunks
				// TODO write to highway file before deleting
				Level.chunks.removeIf((Chunk c) -> iterations - c.lastVisit >= 100000000);
			}
			
			if(Settings.autosave && System.currentTimeMillis()-autosavetimer > 900000) { // Autosave every 15 mins
				LangtonMain.saveState();
				System.out.println("Autosave");
				autosavetimer = System.currentTimeMillis();
			}
		}
		long period = Ant.CYCLEFOUND ? Ant.minHighwayPeriod:(Ant.saveState ? 1:0);
		
		return new long[] {rule,period,iterations};
//		saveRule();
	}
	
	protected static void saveBinHighway(File file) {
		byte[] pixels = new byte[Settings.highwaySizew*Settings.highwaySizeh]; //TODO Use mappedbytebuffer for >= 2GB files  or  calculate the highway size on the fly
		
		try {
//			MyMappedByteBuffer mbb = new MyMappedByteBuffer(file);
//			mbb.put(0, ByteBuffer.allocate(8).putInt(Settings.highwaySizew).putInt(Settings.highwaySizeh).array());
//			System.out.println();
//			Level.renderHighway(mbb, Settings.canvasSize, Settings.highwaySizew, Settings.highwaySizeh, Settings.followAnt);
			Level.renderHighway(pixels, Settings.canvasSize, Settings.highwaySizew, Settings.highwaySizeh, Settings.followAnt);
			FileOutputStream baos = new FileOutputStream(file);
			baos.write(ByteBuffer.allocate(8).putInt(Settings.highwaySizew).putInt(Settings.highwaySizeh).array());
			baos.write(pixels);
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected static void saveImage(long rule, File file) {
//		Simulation.saveBinHighway(new File(Simulation.rule+".bin"));
//		if(0==0) return;
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
	
	public static void toot(long rule) throws IOException{
		if(Settings.toot && Ant.minHighwayPeriod > 100000) {
			//MASTODON BOT mastodon.social/@langtonant
			BufferedReader fr = new BufferedReader(new FileReader("utctimeschedule.txt"));
			long utctime = Long.parseLong(fr.readLine());
			long now = Instant.now().getEpochSecond();
			while(now > utctime+400) {
				utctime += 7200;
			}
			File tmpimg = File.createTempFile("langtonimg", ".png");
			saveImage(rule,tmpimg);
			System.out.println("Toot at " + new SimpleDateFormat().format(new Date(utctime*1000)));
			Runtime.getRuntime().exec("python -c \"from mastodon import Mastodon; from datetime import datetime; m = Mastodon(access_token = '"+System.getenv("MASTODONTOKEN")+"', api_base_url = 'https://mastodon.social/'); m.status_post('Rule: "+Rule.string()+"\\nPeriod: "+Ant.minHighwayPeriod+"', media_ids=[m.media_post(r'"+tmpimg.getPath()+"')], scheduled_at=datetime.utcfromtimestamp("+utctime+"))\"");
			fr.close();
			FileWriter fw = new FileWriter("utctimeschedule.txt", false);
			fw.write(utctime+7200+"");
			fw.close();
		}
	}
	
	public static void saveRule(long rule) {
		try {
			if(Ant.CYCLEFOUND) {
				toot(rule);
				if(Settings.savepic) {
					File dir = new File(Ant.minHighwayPeriod+"");
					boolean newdir = !dir.exists() ? dir.mkdir():false;
					saveImage(rule, new File(Ant.minHighwayPeriod + "/"+rule+".png"));
				}
			} else if(Ant.saveState) {
				if(Settings.savepic) saveImage(rule, new File(0 + "/" + rule+".png"));
			}
			long period = Ant.CYCLEFOUND ? Ant.minHighwayPeriod:(Ant.saveState ? 1:0);
//			FileOutputStream fos = new FileOutputStream(Settings.file, true);
//			fos.write(ByteBuffer.allocate(16).putLong(rule).putLong(period).array());
//			fos.close();
			
//			if(savedRules!=null) {
//				int index = Collections.binarySearch(savedRules, rule);
//				if(index < 0) savedRules.add(-index-1, rule);				
//			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}