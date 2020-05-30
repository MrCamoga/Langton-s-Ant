package com.camoga.ant;

import java.util.Random;

public class Rule {
	
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
		colors = new int[size];
		turn = new int[size];
		long seed = -8485983343335656213L;
		Random r = new Random();
		for(int i = 0; i < colors.length; i++) {
			boolean right = rule%2 != 0;
			rule = rule>>1;
			colors[i] = r.nextInt(0x1000000);
			turn[i] = right ? 1:-1;
		}
		if(colors.length > 64) throw new RuntimeException("More than 64 states not supported");
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
}