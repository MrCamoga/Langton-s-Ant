package com.camoga.ant.ants;

import java.util.Random;

public class HexRule implements IRule {
	
	public int[] colors;
	public int[] turn;
	public long rule;
	public byte size;
	
	private static final String[] letters = new String[] {"F","R","S","B","P","L"};
	
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
		size = (byte) (Math.log(rule)/Math.log(6)+1);
		if(size > 32) throw new RuntimeException("More than 32 states not supported");
		colors = new int[size];
		turn = new int[size];
		Random r = new Random();
		for(int i = 0; i < size; i++) {
			colors[i] = r.nextInt(0x1000000);
			turn[i] = (int) (rule%6);
			rule /= 6;
		}
	}
	
	/**
	 * Returns rule string currently being simulated
	 * @return
	 */
	public String string() {
		String rule = "";
		for(int i = 0; i < turn.length; i++) {
			rule += letters[turn[i]];
		}
		return rule;
	}
	
	public long getRule() {
		return rule;
	}
	
	public int[] getColors() {
		return colors;
	}

	public int getSize() {
		return size;
	}

	public int get(int index) {
		return turn[index];
	}
}