package com.camoga.ant.ants;

import java.util.Random;

public class Rule3D extends AbstractRule {
	
	private static final String[] letters 	= {"R","L","U","D"};
	private static final int[] rotations 	= { 0, 1, 2, 3}; 
	
	public void createRule(long rule) {		
		this.rule = rule;
		this.size = 32-Long.numberOfLeadingZeros(rule)/2;
//		rule += 1<<(size*2);
		colors = new int[size+1];
		turn = new int[size];
		letter = new int[size];
		Random r = new Random();
		for(int i = 0; rule != 0; i++) {
			letter[i] = (int) (rule&3);
			turn[i] = rotations[letter[i]];
			rule >>>= 2;
			colors[i] = r.nextInt(0x1000000);
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