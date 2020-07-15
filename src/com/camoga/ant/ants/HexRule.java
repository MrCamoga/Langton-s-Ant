package com.camoga.ant.ants;

import java.util.Random;

public class HexRule extends AbstractRule {
	
	private static final String[] letters = {"R1","R2","U","L2","L1","N"};
	
	public void createRule(long rule) {
		this.rule = rule;
		// size = (byte) (Math.log(rule)/Math.log(6)+1);
		colors = new int[25];
		turn = new int[25];
		Random r = new Random();
		for(size = 0; rule != 0; size++) {
			colors[size] = r.nextInt(0x1000000);
			turn[size] = (int) (rule%6);
			rule /= 6;
		}
		if(size > 25) throw new RuntimeException("More than 32 states not supported");
	}
	
	/**
	 * Returns rule string currently being simulated
	 * @return
	 */
	public String string() {
		String rule = "";
		for(int i = 0; i < size; i++) {
			rule += letters[turn[i]];
		}
		return rule;
	}
}