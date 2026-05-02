package com.camoga.ant.strategies;

public class SequentialStrategy implements StrategyInterface {

	protected long start, step, limit, current;
	protected boolean overflow = false;

	public SequentialStrategy() {}

	public SequentialStrategy(long start, long step, long limit) {
		this.start = start;
		this.step = step;
		this.limit = limit;
		this.current = start;
	}

	public SequentialStrategy(long start, long step) {
		this(2,1, -1);
	}

	public SequentialStrategy(long start) {
		this(2,1);
	}

	@Override
	public synchronized Long next() {
		if(overflow || Long.compareUnsigned(limit, current) < 0) return null;
		long res = current;
		current += step;
		if(Long.compareUnsigned(current, res) < 0) overflow = true;
		return res;
	}
}
