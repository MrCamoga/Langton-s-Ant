package com.camoga.ant.ants;

import java.util.Random;

public class Rule4D extends AbstractRule {
	
	private static final String[] letters 	= {"R","L","U","D","X","Y"}; // RL xy-plane rotation, UD xz-plane rotation, XY xw-plane rotation
	private static final int[] rotations 	= {100, 52, 99, 29, 97, 22}; 
	
	public void createRule(long rule) {
		this.rule = rule;
		this.size = 0;
//		rule += 1<<(size*2);
		colors = new int[33];
		turn = new int[32];
		letter = new int[32];
		Random r = new Random();
		for(size = 0; rule != 0; size++) {
			letter[size] = (int) Long.remainderUnsigned(rule, 6);
			turn[size] = rotations[letter[size]];
			rule = Long.divideUnsigned(rule, 6);
			colors[size] = r.nextInt(0x1000000);
		}
		colors[0] = 0xff101010;
		colors[size] = colors[0];
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