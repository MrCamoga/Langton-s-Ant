package com.camoga.ant;

import com.camoga.ant.test.hex.AbstractAnt;

public class Ant extends AbstractAnt {
	
	static final int[][] directions = new int[][] {{0,-1},{1,0},{0,1},{-1,0}};
	
	
	public Ant(Worker worker) {
		super(worker);
		rule = new Rule();
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
			dir = (dir + rule.get(state))&0b11;
			if(++chunk.cells[index] == rule.getSize()) chunk.cells[index] = 0;
			
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

	public void initPeriodFinding() {
		states[0] = (byte)(dir<<6 | state);
	}
}