package com.camoga.ant.ants;

public class RuleHex extends AbstractRule {
	
	private static final String[] letters = {"F","R","r","B","l","L"};
	
	public void createRule(long rule) {
		this.rule = rule;
		turn = new int[65536];
		turn[0] = (int) (Long.remainderUnsigned(rule, 3)+1);
		rule = Long.divideUnsigned(rule, 3);
		for(size = 1; rule != 0; size++) {
			turn[size] = (int) (Long.remainderUnsigned(rule, 6));
			rule = Long.divideUnsigned(rule, 6);
		}
		if(size > 32) throw new RuntimeException("More than 32 states not supported");
		for(int i = size; i < turn.length; i++) {
			turn[i] = turn[i-size];
		}
		generateString();
	}
	
	private String generateString() {
		ruleString = "";
		for(int i = 0; i < size; i++) {
			ruleString += letters[turn[i]];
		}
		return ruleString;
	}
}