package com.camoga.ant;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Rule {
	
	public static List<CellColor> colors;
	
	public static class CellColor {
		int color;
		boolean right;
		
		CellColor(int color, boolean right) {
			this.color = color;
			this.right = right;
		}
	}
	
	public static void createRule(long rule) {
		colors = new ArrayList<CellColor>();
		long seed = -8485983343335656213L;
		Random r = new Random(seed);
		while(rule != 0) {
			boolean right = rule%2 != 0;
			rule = rule>>1;
			colors.add(new CellColor(r.nextInt(0x1000000), right));
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
	public static String string() {
		String rule = "";
		for(int i = 0; i < colors.size(); i++) {
			rule += colors.get(i).right ? "R":"L";
		}
		return rule;
	}
}