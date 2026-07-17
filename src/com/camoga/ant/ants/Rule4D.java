package com.camoga.ant.ants;

public class Rule4D extends AbstractRule {
	
	private static final String[] letters 	= {"R","L","U","D","X","Y"}; // RL xy-plane rotation, UD xz-plane rotation, XY xw-plane rotation
	private static final int[] rotations 	= {0, 1, 2, 3, 4, 5}; 
	
	public void createRule(long rule) {
		this.rule = rule;
		this.size = 0;
//		rule += 1<<(size*2);
		turn = new int[65536];
		letter = new int[32];
		for(; rule != 0; size++) {
			letter[size] = (int) Long.remainderUnsigned(rule, 6);
			turn[size] = rotations[letter[size]];
			rule = Long.divideUnsigned(rule, 6);
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