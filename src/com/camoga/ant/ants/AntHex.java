package com.camoga.ant.ants;

import com.camoga.ant.Settings;
import com.camoga.ant.Worker;
import com.camoga.ant.level.Level.Chunk;

public class AntHex extends AbstractAnt {

	static final int[] directionx = new int[] {0,1,1,0,-1,-1};
	static final int[] directiony = new int[] {-1,-1,0,1,1,0};
	
	public AntHex(Worker worker) {
		super(worker,2);
		rule = new RuleHex();
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
				if(states[match]!=s1) {
					match = 0;
					period = stateindex;
					xend = getX();
					yend = getY();
				} else {
					match++;
					if(match == states.length || match > Settings.repeatpercent*period) {
						PERIODFOUND = true;
						saveState = false;
						break;
					}
				}
			}
		}
		return iterations;
	}
	
	public int computeHash() {
		int hash = 1;
		for(int i = -3, yc = this.yc, y = this.y+i; i < 4; i++, y++) {
			if(y < 0) {
				y += cSIZE;
				yc--;
			} else if(y > cSIZEm) {
				y-= cSIZE;
				yc++;
			}
			for(int j = -3, xc = this.xc, x = this.x+j; j < 4; j++, x++) {
				if(x < 0) {
					x += cSIZE;
					xc--;
				} else if(x > cSIZEm) {
					x-= cSIZE;
					xc++;
				}
				Chunk c = worker.getLevel().getChunk2(xc, yc);
				hash = 31*hash + (c!=null ? c.cells[(y<<cPOW) | x]:0);
			}
		}
		return hash;
	}
}