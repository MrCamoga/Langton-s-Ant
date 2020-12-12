package com.camoga.ant.ants;

import java.util.Arrays;
import java.util.Random;

public class Rule4D extends AbstractRule {
	
	private static final String[] letters 	= {"R","L","U","D","X","Y"}; // RL xy-plane rotation, UD xz-plane rotation, XY xw-plane rotation
	private static final int[] rotations 	= {100, 52, 99, 29, 97, 22}; 
	
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
		this.size = 0;
//		rule += 1<<(size*2);
		colors = new int[32];
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
//		System.out.println(string());
//		System.out.println(Arrays.toString(turn));
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