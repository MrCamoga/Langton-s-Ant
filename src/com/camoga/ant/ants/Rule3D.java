package com.camoga.ant.ants;

import java.util.Random;

public class Rule3D extends AbstractRule {
	
	private static final String[] letters 	= {"R","L","U","D"};
	private static final int[] rotations 	= { 13, 10, 17, 22}; 
	
	public void createRule(long rule) {
		// RLRUUUL
//		letter = new int[] {0,1,0,2,2,2,1};
//		size = letter.length;
//		turn = new int[size];
//		
//		for(int i = 0; i < size; i++) {
//			turn[i] = rotations[letter[i]];
//		}
		
		this.rule = rule;
		this.size = 32-Long.numberOfLeadingZeros(rule)/2;
//		rule += 1<<(size*2);
		colors = new int[size];
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
	}
	
	/**
	 * Returns rule string currently being simulated
	 * @return
	 */
	public String string() {
		String rule = "";
		for(int i = 0; i < size; i++) {
			rule += letters[letter[i]];
		}
		return rule;
	}
}