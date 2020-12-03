package com.camoga.ant.ants;

import com.camoga.ant.Settings;
import com.camoga.ant.Worker;

public class Ant3D extends AbstractAnt {

	static final int[] directionx = new int[] {1,0,0,0,0,-1,0,-1,0,1,0,0,0,0,-1,0,1,0,1,0,0,0,0,-1};
	static final int[] directiony = new int[] {0,0,-1,1,0,0,0,0,0,0,1,-1,1,-1,0,0,0,0,0,0,-1,1,0,0};
	static final int[] directionz = new int[] {0,1,0,0,-1,0,-1,0,1,0,0,0,0,0,0,-1,0,1,0,1,0,0,-1,0};
	
	// Cayley table for the symmetric group on four letters (S4)
	static final int[] transform = {
			0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,0,0,0,0,0,0,0,0,
			1,0,4,5,2,3,7,6,10,11,8,9,18,19,20,21,22,23,12,13,14,15,16,17,0,0,0,0,0,0,0,0,
			2,3,0,1,5,4,12,13,14,15,16,17,6,7,8,9,10,11,19,18,22,23,20,21,0,0,0,0,0,0,0,0,
			3,2,5,4,0,1,13,12,16,17,14,15,19,18,22,23,20,21,6,7,8,9,10,11,0,0,0,0,0,0,0,0,
			4,5,1,0,3,2,18,19,20,21,22,23,7,6,10,11,8,9,13,12,16,17,14,15,0,0,0,0,0,0,0,0,
			5,4,3,2,1,0,19,18,22,23,20,21,13,12,16,17,14,15,7,6,10,11,8,9,0,0,0,0,0,0,0,0,
			6,7,8,9,10,11,0,1,2,3,4,5,14,15,12,13,17,16,20,21,18,19,23,22,0,0,0,0,0,0,0,0,
			7,6,10,11,8,9,1,0,4,5,2,3,20,21,18,19,23,22,14,15,12,13,17,16,0,0,0,0,0,0,0,0,
			8,9,6,7,11,10,14,15,12,13,17,16,0,1,2,3,4,5,21,20,23,22,18,19,0,0,0,0,0,0,0,0,
			9,8,11,10,6,7,15,14,17,16,12,13,21,20,23,22,18,19,0,1,2,3,4,5,0,0,0,0,0,0,0,0,
			10,11,7,6,9,8,20,21,18,19,23,22,1,0,4,5,2,3,15,14,17,16,12,13,0,0,0,0,0,0,0,0,
			11,10,9,8,7,6,21,20,23,22,18,19,15,14,17,16,12,13,1,0,4,5,2,3,0,0,0,0,0,0,0,0,
			12,13,14,15,16,17,2,3,0,1,5,4,8,9,6,7,11,10,22,23,19,18,21,20,0,0,0,0,0,0,0,0,
			13,12,16,17,14,15,3,2,5,4,0,1,22,23,19,18,21,20,8,9,6,7,11,10,0,0,0,0,0,0,0,0,
			14,15,12,13,17,16,8,9,6,7,11,10,2,3,0,1,5,4,23,22,21,20,19,18,0,0,0,0,0,0,0,0,
			15,14,17,16,12,13,9,8,11,10,6,7,23,22,21,20,19,18,2,3,0,1,5,4,0,0,0,0,0,0,0,0,
			16,17,13,12,15,14,22,23,19,18,21,20,3,2,5,4,0,1,9,8,11,10,6,7,0,0,0,0,0,0,0,0,
			17,16,15,14,13,12,23,22,21,20,19,18,9,8,11,10,6,7,3,2,5,4,0,1,0,0,0,0,0,0,0,0,
			18,19,20,21,22,23,4,5,1,0,3,2,10,11,7,6,9,8,16,17,13,12,15,14,0,0,0,0,0,0,0,0,
			19,18,22,23,20,21,5,4,3,2,1,0,16,17,13,12,15,14,10,11,7,6,9,8,0,0,0,0,0,0,0,0,
			20,21,18,19,23,22,10,11,7,6,9,8,4,5,1,0,3,2,17,16,15,14,13,12,0,0,0,0,0,0,0,0,
			21,20,23,22,18,19,11,10,9,8,7,6,17,16,15,14,13,12,4,5,1,0,3,2,0,0,0,0,0,0,0,0,
			22,23,19,18,21,20,16,17,13,12,15,14,5,4,3,2,1,0,11,10,9,8,7,6,0,0,0,0,0,0,0,0,
			23,22,21,20,19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1,0,0,0,0,0,0,0,0,0,
	};
	
	public Ant3D(Worker worker) {
		super(worker);
		rule = new Rule3D();
	}
	
	public void init(long rule, long iterations) {
		super.init(rule, iterations);
		repeatLength = 2;
		states[2] = -1;
	}

	public int move() {
		int iteration = 0;
		for(; iteration < Settings.itpf; iteration++) {
			changechunk: {
				if(x > worker.getLevel().cSIZEm) {
					x = 0;
					xc++;
				} else if(x < 0) {
					x = worker.getLevel().cSIZEm;
					xc--;
				} else if(y > worker.getLevel().cSIZEm) {
					y = 0;
					yc++;
				} else if(y < 0) {
					y = worker.getLevel().cSIZEm;
					yc--;
				} else if(z > worker.getLevel().cSIZEm) {
					z = 0;
					zc++;
				} else if(z < 0) {
					z = worker.getLevel().cSIZEm;
					zc--;
				} else break changechunk;
				chunk = worker.getLevel().getChunk(xc, yc, zc);
			}
			
			int index = (((z<<worker.getLevel().cPOW)|y)<<worker.getLevel().cPOW)|x;
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
				
				if(states[repeatLength]!=(byte)dir || states[repeatLength+1]!=(byte)state) {
					repeatLength = 0;
					minHighwayPeriod = stateindex;
					xend = getX();
					yend = getY();
					zend = getZ();
				} else {
					repeatLength+=2;
					if(repeatLength == states.length || repeatLength > Settings.repeatcheck*minHighwayPeriod) {
						PERIODFOUND = true;
						saveState = false;
						minHighwayPeriod/=2;
						break;
					}
				}
			}
		}
		return iteration;
	}
}