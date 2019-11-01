package com.camoga.ant;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Rule {
	
	public static List<CellColor> colors;
	static Random r;
	
	public class CellColor {
		int color;
		boolean right;
		
		CellColor(int color, boolean right) {
			this.color = color;
			this.right = right;
		}
	}
	
	public Rule(long rule) {
		colors = new ArrayList<CellColor>();
		long seed = -8485983343335656213L;
		r = new Random(seed);
		while(rule != 0) {
			boolean right = rule%2 != 0;
			rule = (rule - (right ? 1:0))/2;
			colors.add(new CellColor(r.nextInt(0x1000000), right));
		}
	}
	
	public static String string() {
		String rule = "";
		for(int i = colors.size()-1; i >= 0; i--) {
			if(colors.get(i).right) rule += "R";
			else rule += "L";
		}
		return rule;
	}
}