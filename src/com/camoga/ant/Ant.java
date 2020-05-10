package com.camoga.ant;

import com.camoga.ant.Level.Chunk;

public class Ant {
	static int dir;
	static int xc,yc;
	static int x, y;

	static Chunk chunk;
	static int state = 0;
	
	static final int[][] directions = new int[][] {{0,-1},{1,0},{0,1},{-1,0}};
	
	static byte[] states;
	
	public static void init() {
		if(states == null) states = new byte[Settings.maxiterations == -1 ? 200000000:(int) Math.min(Settings.maxiterations/Settings.repeatcheck, 200000000)];
		x = 0;
		y = 0;
		xc = 0;
		yc = 0;
		dir = 0;
		state = 0;
		saveState = false;
		repeatLength = 0;
		index = 1;
		minHighwayPeriod = 0;
		CYCLEFOUND = false;
		chunk = Level.chunks.get(0);
	}
	
	/**
	 * 
	 * @return true if ant forms a highway
	 */
	public static int move() {
		int i = 0;
		for(; i < Settings.itpf; i++) {
			if(checkCycle(dir, state)) break;
			
			if(x > Settings.cSIZEm) { //Only get chunk when changed
				x = 0;
				xc++;
				chunk = Level.getChunk(xc, yc);
			} else if(x < 0) {
				x = Settings.cSIZEm;
				xc--;
				chunk = Level.getChunk(xc, yc);
			} else if(y > Settings.cSIZEm) {
				y = 0;
				yc++;
				chunk = Level.getChunk(xc, yc);
			} else if(y < 0) {
				y = Settings.cSIZEm;
				yc--;
				chunk = Level.getChunk(xc, yc);
			}
			
			int index = x|(y<<Settings.cPOW);
			state = chunk.cells[index];
			boolean right = Rule.colors[state].right;
			dir = (dir + (right ? 1:-1))&0b11;
			if(++chunk.cells[index] == Rule.size) chunk.cells[index] = 0;
			
			x += directions[dir][0];
			y += directions[dir][1];
			
			//OPTIMIZE (chunk coordinates can only change if x/y = 0/cSIZE)
//			xc += x>>Settings.cPOW;
//			yc += y>>Settings.cPOW;
//			x = x&Settings.cSIZEm;
//			y = y&Settings.cSIZEm;
		}
		return i;
	}
	
	public static boolean saveState = false;
	public static int repeatLength = 0;
	public static long index = 1;
	
	public static long minHighwayPeriod = 0;  // This is the final cycle length
	public static boolean CYCLEFOUND = false;
	
	private static boolean checkCycle(int dir, int state) {
		if(!saveState) return false;
		byte s1 = (byte)(dir<<6 | state); //Only works for rules with <= 64 colors
		if(index < states.length) states[(int) index] = s1;
		index++;
		if(states[repeatLength]==s1) repeatLength++;
		else {
			repeatLength = 0;
			minHighwayPeriod = index;
			return false;
		}

		if(repeatLength == states.length || repeatLength > Settings.repeatcheck*minHighwayPeriod) {
			CYCLEFOUND = true;
			saveState = false;
			return true;
		}
		return false;
	}
}