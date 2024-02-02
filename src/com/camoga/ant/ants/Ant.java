package com.camoga.ant.ants;

import com.camoga.ant.Settings;
import com.camoga.ant.Worker;
import com.camoga.ant.level.Level.Chunk;

public class Ant extends AbstractAnt {

	static final int[] directionx = new int[] {0,1,0,-1};
	static final int[] directiony = new int[] {-1,0,1,0};
	static int[] directioni;
	
	public Ant(Worker worker) {
		super(worker,2);
		rule = new Rule();
	}
	
	public void init(long rule, long iterations) {
		super.init(rule, iterations);
		directioni = new int[] {-cSIZE,1,cSIZE,-1};
		index = 0;
		dir1 = 0;
		dir2 = 0;
	}
	
	int dir1, dir2, state1, state2, index;
	byte s1, s2;
	
	public int move() {
		int iteration = 0;
		for(; iteration < Settings.itpf; iteration+=2) {
			changechunk: {
				if(y > cSIZEm) {
					y = 0;
					yc++;
				} else if(y < 0) {
					y = cSIZEm;
					yc--;
				} else break changechunk;
				chunk = worker.getLevel().getChunk(xc, yc);
			}
		
			index = (y<<cPOW)|x;
			state1 = chunk.cells[index]++;
			if(chunk.cells[index] == rule.getSize()) chunk.cells[index] = 0;
			direction += rule.turn[state1];
			dir1 = (int)direction&0b11;
			x += directionx[dir1];

			changechunk: {
				if(x > cSIZEm) {
					x = 0;
					xc++;
				} else if(x < 0) {
					x = cSIZEm;
					xc--;
				} else break changechunk;
				chunk = worker.getLevel().getChunk(xc, yc);
			}

			index = (y<<cPOW)|x;
			state2 = chunk.cells[index]++;
			if(chunk.cells[index] == rule.getSize()) chunk.cells[index] = 0;
			direction += rule.turn[state2];
			dir2 = (int)direction&0b11;
			y += directiony[dir2];
			
			if(findingPeriod()) {
				s1 = (byte)(state1<<2 | dir1);
				s2 = (byte)(state2<<2 | dir2);
				if(stateindex < states.length) {
					states[(int) stateindex++] = s1;
					states[(int) stateindex++] = s2;
				} else stateindex+=2;

				if(states[match] != s1 || states[match+1] != s2) {
					match = 0;
					period = stateindex;
					xend = getX();
					yend = getY();
					directionend = direction;
				} else {
					match += 2;
					if(match == states.length || match > 50000+Settings.repeatpercent*period) {
						PERIODFOUND = true;
						saveState = false;
						break;
					}
				}
			}
		}
		return iteration;
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