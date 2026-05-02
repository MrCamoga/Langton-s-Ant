package com.camoga.ant.results;

import java.util.ArrayList;

import com.camoga.ant.Worker;
import com.camoga.ant.WorkerManager;
import com.camoga.ant.ants.AbstractAnt;
import com.camoga.ant.ants.ResultSet;
import com.camoga.ant.strategies.StrategyInterface;

public abstract class Result {

	protected int type;

	protected int workerCount = 1;
	protected ArrayList<Worker> workers = new ArrayList<Worker>();

	protected StrategyInterface strategy;

	protected Result(int type) {
		this.type = type;
		setStrategy(defaultStrategy());
	}

	protected Result() {}

	public abstract void sendResult();

	public abstract ResultSet initAnt(AbstractAnt ant);

	public void startWorkers() {
		for(Worker w : workers) {
			w.start();
		}
	}

	public void addWorkers(int count) {
		workerCount = count;
		for(int i = workers.size(); i < count; i++) {
			workers.add(new Worker(WorkerManager.getId(), this));
		}
	}

	public void removeWorker(Worker worker) {
		workers.remove(worker);
	}

	public int getWorkerCount() {
		return workerCount;
	}

	public int getType() {
		return type;
	}
	
	public void setStrategy(StrategyInterface strategy) {
		this.strategy = strategy;
		strategy.init(this);
	}

	protected abstract StrategyInterface defaultStrategy();
}