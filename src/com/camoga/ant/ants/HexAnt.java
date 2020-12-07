package com.camoga.ant.ants;

import com.camoga.ant.Settings;
import com.camoga.ant.Worker;

public class HexAnt extends AbstractAnt {

	static final int[] directionx = new int[] {0,1,1,0,-1,-1};
	static final int[] directiony = new int[] {-1,-1,0,1,1,0};
	
	public HexAnt(Worker worker) {
		super(worker,2);
		rule = new HexRule();
	}
	
	public void init(long rule, long iterations) {
		super.init(rule, iterations);
	}
	
	/**
	 * 
	 * @return true if ant forms a highway
	 */
	public int move() {
		int iterations = 0;
		for(; iterations < Settings.itpf; iterations++) {
			
			if(x > cSIZEm || y > cSIZEm || x < 0 || y < 0) {
				xc += x >> cPOW;
				x &=cSIZEm;
				yc += y >> cPOW;
				y &= cSIZEm;
				chunk = worker.getLevel().getChunk(xc, yc);
			}
			
			int index = x|(y<<cPOW);
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
					xend = getX();
					yend = getY();
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