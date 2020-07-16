package com.camoga.ant.ants;

import java.util.Arrays;
import java.util.Random;

public class HexRule extends AbstractRule {
	
	private static final String[] letters = {"N","R1","R2","U","L2","L1"};
	
	public void createRule(long rule) {
		this.rule = rule;
		// size = (byte) (Math.log(rule)/Math.log(6)+1);
		colors = new int[32];
		turn = new int[32];
		Random r = new Random();
		turn[0] = (int) (Long.remainderUnsigned(rule, 3)+1);
		colors[0] = r.nextInt();
		rule = Long.divideUnsigned(rule, 3);
		for(size = 1; rule != 0; size++) {
			colors[size] = r.nextInt(0x1000000);
			turn[size] = (int) (Long.remainderUnsigned(rule, 6));
			rule = Long.divideUnsigned(rule, 6);
		}
		if(size > 32) throw new RuntimeException("More than 32 states not supported");
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