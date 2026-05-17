package com.camoga.ant;

import java.util.ArrayList;

import com.camoga.ant.results.Result;
import com.camoga.ant.results.ResultRules;
import static com.camoga.ant.Main.LOG;;

public class WorkerManager {
	
	protected static ArrayList<Result> results = new ArrayList<Result>();
	protected static int numworkers = 0;

	static int idcount;

	public static void add(Result result) {
		int threads = Runtime.getRuntime().availableProcessors();
		if(threads < numworkers + result.getWorkerCount()) {
			LOG.warning("More workers than available processors ("+threads+")");
			return;
		}
		results.add(result);
		numworkers += result.getWorkerCount();
	}

	public static int getNumWorkers() {
		return numworkers;
	}
	
	// public static void remove(Worker worker) {
	// 	workers.remove(worker);
	// }
	
	// public static void setWorkerType(int type, int num) {
	// 	if(num < 0 || WorkerManager.size() + numworkers[type] - num > Runtime.getRuntime().availableProcessors()) throw new RuntimeException();
	// 	numworkers[type] = num;
	// 	updateWorkers();
	// }
	
	// public static void setWorkers(int normal, int hex, int r3, int r4) {
	// 	if(normal < 0 || hex < 0 || r3 < 0 || r4 < 0 || normal + hex + r3 + r4 > Runtime.getRuntime().availableProcessors()) throw new RuntimeException("More workers than available processors ("+Runtime.getRuntime().availableProcessors()+")");
	// 	numworkers[0] = normal;
	// 	numworkers[1] = hex;
	// 	numworkers[2] = r3;
	// 	numworkers[3] = r4;
	// 	updateWorkers();
	// }

	public static synchronized int getId() {
		return idcount++;
	}
	
	public static void start() {
		for(Result result : results) {
			result.startWorkers();
		}
	}
	
	public static int size() {
		int count = 0;
		for(Result result : results) {
			count += result.getWorkerCount();
		}
		return count;
	}
	
	public static int size(int type) {
		int count = 0;
		for(Result result : results) {
			if(result.getType() == type)
				count += result.getWorkerCount();
		}
		return count;
	}

	public static <T extends Result> T getResult(int type, Class<T> resultType) {
		for(Result result : results) {
			if(resultType.isInstance(result) && result.getType() == type) resultType.cast(result);
		}
		return null;
	}

	// public static Worker getWorker(int id) {
	// 	if(id < 0 || id >= workers.size()) return null;
	// 	return workers.get(id);
	// }
}