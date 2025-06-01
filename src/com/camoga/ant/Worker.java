package com.camoga.ant;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.math3.random.MersenneTwister;

import com.camoga.ant.ants.AbstractAnt;
import com.camoga.ant.ants.Ant;
import com.camoga.ant.ants.Ant3D;
import com.camoga.ant.ants.Ant4D;
import com.camoga.ant.ants.AntHex;
import com.camoga.ant.ants.Map;
import com.camoga.ant.ants.ResultSet;
import com.camoga.ant.ants.patterns.PatternRandom;
import com.camoga.ant.net.Client;

public class Worker {
	
	Thread thread;	
	boolean kill;
	boolean running;
	int workerid;
	
//	boolean recordingVideo = false;
//	VideoCreator video;
	
	long autosavetimer;
	AbstractAnt ant;
	int type;
	
	public Worker(int ID, int type) {
		this.workerid = ID;
		this.type = type;
		if(type==0) ant = new Ant();
		else if(type==1) ant = new AntHex();
		else if(type==2) ant = new Ant3D();
		else if(type==3) ant = new Ant4D();
		else throw new IllegalArgumentException("Invalid type of ant");
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
		Random rand = new Random(8932);
		int[] numseed = new int[4];
		String seed = "";
		for(int i = 0, x; i < 3; i++) {
			numseed[i] = x = rand.nextInt(916132832);
			for(int j = 0; j < 5; j++) {
				int q = x/62;
				int r = x-62*q;
				x = q;
				if(r < 10) seed += (char) (r+'0');
				else if(r < 36) seed += (char) (r-10+'A');
				else seed += (char) (r-36+'a');
			}
		}
		System.out.println(seed);
		int index = 0;
		//long rule = 8;
		//ResultSoup soupresult = new ResultSoup(0, rule, seed);
		while(true || !kill) {
			long rule = 31819;
			long maxiterations = 100000000;
			numseed[numseed.length-1] = index;
			
			time = System.nanoTime();
			ResultSet result = ant.run(rule,maxiterations);


			float seconds = (float) ((-time + (time = System.nanoTime()))/1e9);
			Client.LOG.info(String.format("%02d %.4E it/s\t%.4f s\t%s", workerid, result.iterations/seconds, seconds, result));
		}
		Client.LOG.warning("Worker " + workerid + " has stopped");
		running = false;
		if(kill) WorkerManager.remove(this);
	}

	
// 	public ResultSet runRule(long rule, long maxiterations, int[] seed) {
// 		long max = maxiterations;
// 		boolean extended = false;
// //		ant.init(rule, maxiterations, new PatternRandom(3, seed));
		
// 		if(ant.getType() == 0) { // 2d ant square grid
			
// 			return new long[] {rule,period,ant.getIterations(),hash,d[1],d[0],winding>>2,ant.maxstate};
// 		} else if(ant.getType() == 1) { // hex ant
// 			if(ant.findingPeriod()) return new long[] {rule,0,ant.getIterations(),hash,1,0};
// 			if(!ant.periodFound()) return new long[] {rule,0,ant.getIterations(),hash,0,0};
// 			// Find the shortest path to dx,dy. We convert to cube coordinates and pick the ones with the same sign
// 			long[] coordinates = {ant.xend-ant.xstart,ant.yend-ant.ystart,0}; coordinates[2] = -coordinates[1] - coordinates[0];
// 			Arrays.sort(coordinates);
// 			if(coordinates[1] >= 0) return new long[] {rule,ant.getPeriod(),ant.getIterations(),hash,coordinates[2],coordinates[1]};
// 			return new long[] {rule,ant.getPeriod(),ant.getIterations(),hash,-coordinates[0],-coordinates[1]};
// 		} else if(ant.getType() == 2) { // 3d ant
// 			long period = ant.periodFound() ? ant.getPeriod():(ant.findingPeriod() ? 1:0);
// 			if(period <= 1) return new long[] {rule,period,ant.getIterations(),hash,0,0,0};
// 			long[] d = {Math.abs(ant.xend-ant.xstart), Math.abs(ant.yend-ant.ystart), Math.abs(ant.zend-ant.zstart)};
// 			Arrays.sort(d);
// 			return new long[] {rule,period,ant.getIterations(),hash,d[2],d[1],d[0]};	
// 		} else if(ant.getType() == 3) { // 4d ant
// 			long period = ant.periodFound() ? ant.getPeriod():(ant.findingPeriod() ? 1:0);
// 			if(period <= 1) return new long[] {rule,period,ant.getIterations(),hash,0,0,0,0};
// 			long[] d = {Math.abs(ant.xend-ant.xstart), Math.abs(ant.yend-ant.ystart), Math.abs(ant.zend-ant.zstart), Math.abs(ant.wend-ant.wstart)};
// 			Arrays.sort(d);
// 			return new long[] {rule,period,ant.getIterations(),hash,d[3],d[2],d[1],d[0]};	
// 		}
// 		return null;
// 	}
	
	protected void saveImage(File file, boolean info) {
		file.mkdirs();
		BufferedImage image = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);
		ant.map.render(image, ((DataBufferInt)(image.getRaster().getDataBuffer())).getData(), image.getWidth(), image.getHeight(), true, info);
		
		try {
			ImageIO.write(image, "PNG", file);
			System.out.println(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public AbstractAnt getAnt() {
		return ant;
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
