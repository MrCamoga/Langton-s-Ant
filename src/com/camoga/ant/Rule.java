package com.camoga.ant;

import java.util.Random;

public class Rule {
	
	public static CellColor[] colors;
	public static long rule;
	public static byte size;
	
	public static class CellColor {
		int color;
		boolean right;
		
		CellColor(int color, boolean right) {
			this.color = color;
			this.right = right;
		}
	}
	
	public static void createRule(long rule) {
		Rule.rule = rule;
		colors = new CellColor[(int) (Math.log(rule)/Math.log(2)+1)];
		size = (byte) colors.length;
		long seed = -8485983343335656213L;
		Random r = new Random();
		for(int i = 0; i < colors.length; i++) {
			boolean right = rule%2 != 0;
			rule = rule>>1;
			colors[i] = new CellColor(r.nextInt(0x1000000), right);
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
	public static String string() {
		String rule = "";
		for(int i = 0; i < colors.length; i++) {
			rule += colors[i].right ? "R":"L";
		}
		return rule;
	}
}