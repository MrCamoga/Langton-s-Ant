package com.camoga.ant;

import java.util.ArrayList;

import com.camoga.ant.net.Client;

public class WorkerManager {

	static ArrayList<Worker> workers = new ArrayList<Worker>();
	static int[] numworkers = new int[Client.ANT_TYPES]; // 0: normal, 1: hex, 2: 3D
	
	static int idcount;
	
	//TODO do not start new workers until old workers have stopped
	private static void updateWorkers() {
		int[] count = new int[Client.ANT_TYPES];
		for(Worker w : workers) {
			count[w.getType()]++;
		}
		for(int i = 0; i < Client.ANT_TYPES; i++) {
			for(Worker w : workers) {
				if(count[i] <= numworkers[i]) break;
				if(w.getType() == i) {
					w.kill();
					count[i]--;
				}
			}
			for(int j = count[i]; j < numworkers[i]; j++) {
				workers.add(new Worker(idcount++, i));
			}
		}
	}
	
	public static void remove(Worker worker) {
		workers.remove(worker);
	}
	
	public static void setWorkerType(int type, int num) {
		if(num < 0 || WorkerManager.size() + numworkers[type] - num > Runtime.getRuntime().availableProcessors()) throw new RuntimeException();
		numworkers[type] = num;
		updateWorkers();
	}
	
	public static void setWorkers(int normal, int hex, int r3, int r4) {
		if(normal < 0 || hex < 0 || r3 < 0 || r4 < 0 || normal + hex + r3 + r4 > Runtime.getRuntime().availableProcessors()) throw new RuntimeException("More workers than available processors ("+Runtime.getRuntime().availableProcessors()+")");
		numworkers[0] = normal;
		numworkers[1] = hex;
		numworkers[2] = r3;
		numworkers[3] = r4;
		updateWorkers();
	}
	
	public static void start() {
		for(Worker w : workers) {
			w.start();
		}
	}
	
	public static int size() {
		return numworkers[0]+numworkers[1]+numworkers[2]+numworkers[3];
	}
	
	public static int size(int type) {
		return numworkers[type];
	}

	public static Worker getWorker(int id) {
		if(id < 0 || id >= workers.size()) return null;
		return workers.get(id);
	}
}