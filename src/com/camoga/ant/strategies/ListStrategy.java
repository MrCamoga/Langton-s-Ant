package com.camoga.ant.strategies;

public class ListStrategy implements StrategyInterface {
	protected long[] rules;
	protected int index = 0;

	public ListStrategy(long[] rules) {
		this.rules = rules;
	}

	@Override
	public synchronized Long next() {
		if(index == rules.length) return null;
		return rules[index++];
	}
}