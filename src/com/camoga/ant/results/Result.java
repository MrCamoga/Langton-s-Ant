package com.camoga.ant.results;

import com.camoga.ant.ants.AbstractAnt;
import com.camoga.ant.ants.ResultSet;

public abstract class Result {

	protected int type;

	protected Result(int type) {
		this.type = type;
	}

	protected Result() {}

	public abstract void sendResult();

	public abstract ResultSet initAnt(AbstractAnt ant);

	public int getType() {
		return type;
	}
}