package com.camoga.ant.strategies;

import com.camoga.ant.results.Result;

public interface StrategyInterface {
	/**
	 * 
	 * @return next rule to simulate, null if finished
	 */
	public Long next();

	public default void init(Result result) {}

	public default int remaining() { return Integer.MAX_VALUE; }
}
