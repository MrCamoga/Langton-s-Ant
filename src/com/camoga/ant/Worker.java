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
import com.camoga.ant.results.ResultSoupRestore;

public class Worker {
	
	Thread thread;	
	boolean kill;
	boolean running;
	int workerid;
	
	long autosavetimer;
	AbstractAnt ant;
	int type;
	public static Result workresult;

	static { // rule 374601147 main period always breaks
		// workresult = new ResultSoup(0, 43, new int[] { 904527579, 868801032, 406085901, 0 }, 5, 30000000, 500000, 455); // para testear algoritmo de calcular periodo
		// workresult = new ResultSoup(0, 43, null, 5, 100000000, 1000000, 0);
		workresult = new ResultSoupRestore("saves/fTuNqWUaYdhaIe7.langton");
		// System.out.println(((ResultSoup)workresult).getSeedString());
		//workresult = new ResultRules(0);
		// workresult = new ResultRulesTest(0, 31819, 150000000);
		// workresult = new ResultSoupRestore(0, 371047772, new int[] {549072545, 903439913, 332504472, 0}, 5, 100000000, 1000000, 0, "371047772soups1M.csv"); // generateSeed()
	}

	public Worker(int ID, Result result) {
		this(ID, result.getType());
		workresult = result;
	}

	public Worker(int ID, int type) {
		this.workerid = ID;
		this.type = type;
		ant = AntFactory.createAnt(type);
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
			ResultSet result = workresult.initAnt(ant);	
			if(result == null) break;	

			//Client.LOG.info("Chunk neighbour hits: " + (ant.map.total-ant.map.nohit) + "/" + ant.map.total + ", " + (1-ant.map.nohit/(double)ant.map.total));
			float seconds = (float) ((-time + (time = System.nanoTime()))/1e9);
			if(result.isNew()) {
				String resultStr = result.toString();
				resultStr = resultStr.substring(0, Math.min(resultStr.length(),300));
				Main.LOG.info(String.format("%02d %.4E it/s\t%.4f s\t%s", workerid, result.iterations/seconds, seconds, result));
			}
		}
		Main.LOG.warning("Worker " + workerid + " has stopped");
		running = false;
		// TODO release ant memory
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
