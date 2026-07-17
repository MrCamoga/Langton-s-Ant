package com.camoga.ant.ants;

public class Rule extends AbstractRule {
	
	public void createRule(long rule) {
		this.rule = rule;
		this.size = (64-Long.numberOfLeadingZeros(rule));
		turn = new int[65535];
		for(int i = 0; rule != 0; i++) {
			turn[i] = (rule&1) == 1 ? 1:-1;
			rule >>>= 1;
		}
		for(int i = size; i < turn.length; i++) {
			turn[i] = turn[i-size];
		}
		generateString();
	}
	
	private String generateString() {
		ruleString = "";
		for(int i = 0; i < size; i++) {
			ruleString += turn[i] == 1 ? "R":"L";
		}
		return ruleString;
	}
}
