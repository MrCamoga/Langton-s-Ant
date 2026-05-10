package com.camoga.ant.ants;

import com.camoga.ant.Settings;
import com.camoga.ant.ants.Map.Chunk;

public class Ant3D extends AbstractAnt {
	
	static final int[] directionx = new int[] {1,0,0,0,0,-1,0,-1,0,1,0,0,0,0,-1,0,1,0,1,0,0,0,0,-1};
	static final int[] directiony = new int[] {0,0,-1,1,0,0,0,0,0,0,1,-1,1,-1,0,0,0,0,0,0,-1,1,0,0};
	static final int[] directionz = new int[] {0,1,0,0,-1,0,-1,0,1,0,0,0,0,0,0,-1,0,1,0,1,0,0,-1,0};
	
	// Reduced cayley table for the symmetric group on four letters (S4)
	static final int[] transform = {
		13,12,16,17,14,15,3,2,5,4,0,1,22,23,19,18,21,20,8,9,6,7,11,10,0,0,0,0,0,0,0,0,
		10,11,7,6,9,8,20,21,18,19,23,22,1,0,4,5,2,3,15,14,17,16,12,13,0,0,0,0,0,0,0,0,
		17,16,15,14,13,12,23,22,21,20,19,18,9,8,11,10,6,7,3,2,5,4,0,1,0,0,0,0,0,0,0,0,
		22,23,19,18,21,20,16,17,13,12,15,14,5,4,3,2,1,0,11,10,9,8,7,6,0,0,0,0,0,0,0,0,
	};
	
	public Ant3D() {
		super(2,3);
		rule = new Rule3D();
	}
	
	public void init(long rule, long iterations) {
		super.init(rule, iterations);
		match = 2;
		states[2] = -1;
	}

	public void move(long it) {
		int iteration = 0;
		for(; iteration < it; iteration++) {
			changechunk: {
				if(x > cSIZEm) {
					x = 0;
					xc++;
				} else if(x < 0) {
					x = cSIZEm;
					xc--;
				} else if(y > cSIZEm) {
					y = 0;
					yc++;
				} else if(y < 0) {
					y = cSIZEm;
					yc--;
				} else if(z > cSIZEm) {
					z = 0;
					zc++;
				} else if(z < 0) {
					z = cSIZEm;
					zc--;
				} else break changechunk;
				chunk = map.getChunk(xc, yc, zc);
			}
			
			int index = (((z<<cPOW)|y)<<cPOW)|x;
			state = chunk.cells[index];

			dir = transform[(rule.turn[state]<<5)|dir];
			
			if(++chunk.cells[index] == rule.getSize()) chunk.cells[index] = 0;
			
			x += directionx[dir];
			y += directiony[dir];
			z += directionz[dir];
			
			if(findingPeriod()) {
				if(stateindex < states.length) {
					states[(int) stateindex++] = (byte) dir;
					states[(int) stateindex++] = (byte) state;
				} else {
					stateindex+=2;
				}
				
				if(states[match]!=(byte)dir || states[match+1]!=(byte)state) {
					match = 0;
					period = stateindex;
					xend = getX();
					yend = getY();
					zend = getZ();
				} else {
					match+=2;
					if(match == states.length || match > Settings.repeatpercent*period) {
						PERIODFOUND = true;
						saveState = false;
						break;
					}
				}
			}
		}
		iterations += iteration;
	}
	
	public int computeHash() {
		int hash = 1;
		for(int i = -2, zc = this.zc, z = this.z+i; i < 3; i++, z++) {
			if(z < 0) {
				z += cSIZE;
				zc--;
			} else if(z > cSIZEm) {
				z-= cSIZE;
				zc++;
			}
			for(int j = -2, yc = this.yc, y = this.y+j; j < 3; j++, y++) {
				if(y < 0) {
					y += cSIZE;
					yc--;
				} else if(y > cSIZEm) {
					y-= cSIZE;
					yc++;
				}
				for(int k = -2, xc = this.xc, x = this.x+k; k < 3; k++, x++) {
					if(x < 0) {
						x += cSIZE;
						xc--;
					} else if(x > cSIZEm) {
						x-= cSIZE;
						xc++;
					}
					Chunk c = map.getChunk2(xc, yc, zc);
					hash = 31*hash + (c!=null ? c.cells[(((z<<cPOW)|y)<<cPOW)|x]:0);
				}
			}
		}
		return hash;
	}
	
	public long getPeriod() {
		return period/2;
	}
}