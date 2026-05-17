package com.camoga.ant;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.camoga.ant.ants.AbstractAnt;
import com.camoga.ant.ants.AntFactory;
import com.camoga.ant.ants.ResultSet;
import com.camoga.ant.results.Result;
import com.camoga.ant.results.ResultRules;

public class Worker {
	
	Thread thread;
	boolean kill;
	boolean running;
	int workerid;
	
	long autosavetimer;
	protected AbstractAnt ant;
	int type;
	protected Result workresult;

	public Worker(int ID, Result result) {
		this(ID, result.getType());
		workresult = result;
	}

	public Worker(int ID, int type) {
		this.workerid = ID;
		this.type = type;
	}
	
	public void start() {
		if(running) return;
		thread = new Thread(() -> run(), "AntWorker"+workerid);
		thread.start();
		Main.LOG.info("Worker " + workerid + " started");
		running = true;
	}

	public void run() {
		long time;
		
		while(!kill) {
			time = System.nanoTime();
			ResultSet result = workresult.initAnt(ant = AntFactory.createAnt(type));	
			if(result == null) break;

			//Client.LOG.info("Chunk neighbour hits: " + (ant.map.total-ant.map.nohit) + "/" + ant.map.total + ", " + (1-ant.map.nohit/(double)ant.map.total));
			float seconds = (float) ((-time + (time = System.nanoTime()))/1e9);
			if(workresult instanceof ResultRules || result.isNew()) {
				String resultStr = result.toString();
				resultStr = resultStr.substring(0, Math.min(resultStr.length(),300));
				Main.LOG.info(String.format("%02d %.4E it/s\t%.4f s\t%s", workerid, result.iterations/seconds, seconds, result));
			}
		}
		Main.LOG.warning("Worker " + workerid + " has stopped");
		running = false;
		if(kill) workresult.removeWorker(this);
	}
	
	protected void saveImage(File file, boolean info) {
		file.mkdirs();
		BufferedImage image = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);
		ant.map.render(image, ((DataBufferInt)(image.getRaster().getDataBuffer())).getData(), image.getWidth(), image.getHeight(), true, info);
		
		try {
			ImageIO.write(image, "PNG", file);
			System.out.println(file);
		} catch(IOException e) {
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
