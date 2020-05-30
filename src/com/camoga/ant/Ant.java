package com.camoga.ant;

import com.camoga.ant.Level.Chunk;

public class Ant {
	int dir;
	int xc,yc;
	int x, y;

	Chunk chunk;
	int state = 0;
	
	static final int[][] directions = new int[][] {{0,-1},{1,0},{0,1},{-1,0}};
	
	byte[] states;
	public boolean saveState = false;
	public int repeatLength = 0;
	public long index = 1;
	
	public long minHighwayPeriod = 0;  // This is the final period length
	public boolean PERIODFOUND = false;
	
	private Worker worker;
	
	public Ant(Worker worker) {
		this.worker = worker;
	}
	
	public void init(long iterations) {
		int stateslen = iterations == -1 ? 200000000:(int) Math.min(Math.max(5000000,iterations/(int)Settings.repeatcheck*2), 200000000);
		if(states == null || states.length != stateslen) states = new byte[stateslen];
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
		PERIODFOUND = false;
		chunk = worker.level.chunks.get(0,0);
	}
	
	/**
	 * 
	 * @return true if ant forms a highway
	 */
	public int move() {
		int i = 0;
		for(; i < Settings.itpf; i++) {			
			if(saveState) {
				byte s1 = (byte)(dir<<6 | state); //Only works for rules with <= 64 colors
				if(index < states.length) states[(int) index] = s1;
				index++;
				if(states[repeatLength]!=s1) {
					repeatLength = 0;
					minHighwayPeriod = index;
				} else {
					repeatLength++;
					if(repeatLength == states.length || repeatLength > Settings.repeatcheck*minHighwayPeriod) {
						PERIODFOUND = true;
						saveState = false;
						break;
					}
				}
			}
			
			if(x > Settings.cSIZEm) {
				x = 0;
				xc++;
				chunk = worker.level.getChunk(xc, yc);
			} else if(x < 0) {
				x = Settings.cSIZEm;
				xc--;
				chunk = worker.level.getChunk(xc, yc);
			} else if(y > Settings.cSIZEm) {
				y = 0;
				yc++;
				chunk = worker.level.getChunk(xc, yc);
			} else if(y < 0) {
				y = Settings.cSIZEm;
				yc--;
				chunk = worker.level.getChunk(xc, yc);
			}
			
			int index = x|(y<<Settings.cPOW);
			state = chunk.cells[index];
			dir = (dir + worker.rule.turn[state])&0b11;
			if(++chunk.cells[index] == worker.rule.size) chunk.cells[index] = 0;
			
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
	
}