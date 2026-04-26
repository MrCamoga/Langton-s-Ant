package com.camoga.ant.results;

import com.camoga.ant.ants.AbstractAnt;
import com.camoga.ant.ants.ResultSet;

public class ResultRulesTest extends Result {
	private long rule;
	private long iterations;

    public ResultRulesTest(int type, long rule, long iterations) {
        super(type);
		this.rule = rule;
		this.iterations = iterations;
    }

	@Override
	public ResultSet initAnt(AbstractAnt ant) {
		ResultSet result = ant.run(rule,iterations,null);
		result.setNew();
		return result;
	}

	@Override
	public void sendResult() {
		throw new UnsupportedOperationException("Unimplemented method 'sendResult'");
	}
}