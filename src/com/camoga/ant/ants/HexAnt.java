package com.camoga.ant.ants;

import com.camoga.ant.Settings;
import com.camoga.ant.Worker;

public class HexAnt extends AbstractAnt {

	static final int[] directionx = new int[] {-1,0,1,1,0,-1};
	static final int[] directiony = new int[] {-1,-1,0,1,1,0};
	
	public HexAnt(Worker worker) {
		super(worker);
		rule = new HexRule();
	}
	
	/**
	 * 
	 * @return true if ant forms a highway
	 */
	public int move() {
		int iterations = 0;
		for(; iterations < Settings.itpf; iterations++) {
			
			if(x > Settings.cSIZEm || y > Settings.cSIZEm || x < 0 || y < 0) {
				xc += x >> Settings.cPOW;
				yc += y >> Settings.cPOW;
				chunk = worker.getLevel().getChunk(xc, yc);
				x &= Settings.cSIZEm;
				y &= Settings.cSIZEm;
			}
			
			int index = x|(y<<Settings.cPOW);
			state = chunk.cells[index];
			dir += rule.turn[state];
			if(dir > 5) dir -= 6;
			if(++chunk.cells[index] == rule.getSize()) chunk.cells[index] = 0;
			
			x += directionx[dir];
			y += directiony[dir];
			if(findingPeriod()) {
				byte s1 = (byte)(dir<<5 | state); //Only works for rules with <= 32 colors
				if(stateindex < states.length) states[(int) stateindex] = s1;
				stateindex++;
				if(states[repeatLength]!=s1) {
					repeatLength = 0;
					minHighwayPeriod = stateindex;
				} else {
					repeatLength++;
					if(repeatLength == states.length || repeatLength > Settings.repeatcheck*minHighwayPeriod) {
						PERIODFOUND = true;
						saveState = false;
						break;
					}
				}
			}
		}
		return iterations;
	}
}