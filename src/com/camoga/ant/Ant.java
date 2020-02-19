package com.camoga.ant;

import com.camoga.ant.Level.Chunk;

public class Ant {
	static int dir;
	static int xc,yc;
	static int x, y;
	
	static final int[][] directions = new int[][] {{0,-1},{1,0},{0,1},{-1,0}};
	
	static byte[] states = new byte[200000000];
	
	public static void init() {
		x = 0;
		y = 0;
		xc = 0;
		yc = 0;
		dir = 0;
		state = 0;
		saveState = false;
		currentCycleLength = 0;
		index = 1;
		minHighwayPeriod = 0;
		CYCLEFOUND = false;
	}
	
	static int state = 0;
	/**
	 * 
	 * @return true if ant forms a highway
	 */
	public static int move() {
		int i = 0;
		for(; i < Settings.itpf; i++) {
			if(checkCycle(dir, state)) break;
			Chunk c = Level.getChunk(xc, yc, true);
			int index = x|(y<<Settings.cPOW);
			state = c.cells[index];
			boolean right = Rule.colors[state].right;
			dir = (dir + (right ? 1:-1))&0b11;
			c.cells[index] = (state+1) % Rule.colors.length;
			
			x += directions[dir][0];
			y += directions[dir][1];
			
			//OPTIMIZE (chunk coordinates can only change if x/y = 0/cSIZE)
			if(x > Settings.cSIZEm) {
				x = 0;
				xc++;
			} else if(x < 0) {
				x = Settings.cSIZEm;
				xc--;
			} else if(y > Settings.cSIZEm) {
				y = 0;
				yc++;
			} else if(y < 0) {
				y = Settings.cSIZEm;
				yc--;
			}
		}
		return i;
	}
	
	static boolean saveState = false;
	public static int currentCycleLength = 0;
	public static long index = 1;
	
	static long minHighwayPeriod = 0;  // This is the final cycle length
	static boolean CYCLEFOUND = false;
	
	private static boolean checkCycle(int dir, int state) {
		if(!saveState) return false;
		byte s1 = (byte)(dir<<6 | state); //Only works for rules with <= 64 colors
		if(index < states.length) states[(int) index] = s1;
		index++;		
		byte s2 = states[currentCycleLength];
		if(s2==s1) currentCycleLength++;
		else {
			currentCycleLength = 0;
			minHighwayPeriod = index;
		}

		if(currentCycleLength == states.length || currentCycleLength > Settings.repeatcheck*minHighwayPeriod) {
			CYCLEFOUND = true;
			saveState = false;
//					System.out.println((x-xs-directions[dir][0]+1)/(Settings.repeatcheck+2)+ ", " + (y-ys-directions[dir][1]+1)/(Settings.repeatcheck+2));
			return true;
		}
		return false;
	}
}