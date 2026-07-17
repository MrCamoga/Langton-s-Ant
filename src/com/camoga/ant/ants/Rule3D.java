package com.camoga.ant.ants;

public class Rule3D extends AbstractRule {
	
	private static final String[] letters 	= {"R","L","U","D"};
	private static final int[] rotations 	= { 0, 1, 2, 3}; 
	
	public void createRule(long rule) {		
		this.rule = rule;
		this.size = 32-Long.numberOfLeadingZeros(rule)/2;
//		rule += 1<<(size*2);
		turn = new int[65536];
		letter = new int[32];
		for(int i = 0; rule != 0; i++) {
			letter[i] = (int) (rule&3);
			turn[i] = rotations[letter[i]] << 5;
			rule >>>= 2;
		}
		for(int i = size; i < turn.length; i++) {
			turn[i] = turn[i-size];
		}
		generateString();
	}
	
	private String generateString() {
		ruleString = "";
		for(int i = 0; i < size; i++) {
			ruleString += letters[letter[i]];
		}
		return ruleString;
	}
}