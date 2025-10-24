package com.camoga.ant;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.collections4.map.MultiKeyMap;
import com.camoga.ant.ants.AbstractAnt;
import com.camoga.ant.ants.Ant;
import com.camoga.ant.ants.Ant3D;
import com.camoga.ant.ants.Ant4D;
import com.camoga.ant.ants.AntHex;
import com.camoga.ant.ants.ResultSet;

public class Worker {
	
	Thread thread;	
	boolean kill;
	boolean running;
	int workerid;
	
	long autosavetimer;
	AbstractAnt ant;
	int type;
	public static Result workresult;

	static {
		// workresult = new ResultSoup(0, 786123, null, 5, 100000000, 10000, 0); // generateSeed()
		// System.out.println(((ResultSoup)workresult).getSeedString());
		workresult = new ResultRules(0);
	}

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
		Main.LOG.info("Worker " + workerid + " started");
		running = true;
	}
	
	MultiKeyMap<Long,Integer> highway_hist = new MultiKeyMap<>();

	public void run() {
		long time;
		
		while(!kill) {
			time = System.nanoTime();
			ResultSet result = workresult.initAnt(ant);		
			if(result == null) break;	

			//Client.LOG.info("Chunk neighbour hits: " + (ant.map.total-ant.map.nohit) + "/" + ant.map.total + ", " + (1-ant.map.nohit/(double)ant.map.total));
			float seconds = (float) ((-time + (time = System.nanoTime()))/1e9);
			Main.LOG.info(String.format("%02d %.4E it/s\t%.4f s\t%s", workerid, result.iterations/seconds, seconds, result));
		}
		Main.LOG.warning("Worker " + workerid + " has stopped");
		running = false;
		if(kill) WorkerManager.remove(this);
	}
	
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
