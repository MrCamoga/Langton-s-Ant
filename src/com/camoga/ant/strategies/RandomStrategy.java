package com.camoga.ant.strategies;

import java.util.Random;

public class RandomStrategy implements StrategyInterface {
	protected Random random;

	public RandomStrategy() {
		random = new Random();
	}

	public RandomStrategy(long seed) {
		random = new Random(seed);
	}

	@Override
	public synchronized Long next() {
		return random.nextLong();
	}
	
}
