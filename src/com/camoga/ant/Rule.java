package com.camoga.ant;

import java.util.Random;

import com.camoga.ant.test.hex.IRule;

public class Rule implements IRule {
	
	public int[] colors;
	public int[] turn;
	public long rule;
	public byte size;
	
	public class CellColor {
		int color;
		boolean right;
		
		CellColor(int color, boolean right) {
			this.color = color;
			this.right = right;
		}
	}
	
	public void createRule(long rule) {
		this.rule = rule;
		size = (byte) (Math.log(rule)/Math.log(2)+1);
		if(size > 64) throw new RuntimeException("More than 64 states not supported");
		colors = new int[size];
		turn = new int[size];
		Random r = new Random();
		for(int i = 0; i < size; i++) {
			turn[i] = (rule&1) == 1 ? 1:3;
			rule >>>= 1;
			colors[i] = r.nextInt(0x1000000);
		}
	}
	
	/**
	 * Returns rule as string of Rs and Ls
	 * @param rule
	 * @return
	 */
	public static String string(long rule) {
		String result = "";
		while(rule != 0) {
			result += rule%2==0 ? "L":"R";
			rule >>= 1;
		}
		return result;
	}
	
	/**
	 * Returns rule string currently being simulated
	 * @return
	 */
	public String string() {
		String rule = "";
		for(int i = 0; i < turn.length; i++) {
			rule += turn[i] == 1 ? "R":"L";
		}
		return rule;
	}
	
	public long getRule() {
		return rule;
	}
	
	public int[] getColors() {
		return colors;
	}
}