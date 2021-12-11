package com.camoga.ant.ants;

import com.camoga.ant.Settings;
import com.camoga.ant.Worker;

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
//		rxt = 0;
//		ryt = 0;
//		rx = 0;
//		ry = 0;
//		rt = 0;
//		r2x = 0;
//		r2y = 0;
//		n = 0;
//		rxx = 0;
//		ryy = 0;
//		rtt = 0;
//		rvx = 100;
//		rvy = 100;
//		regression = true;
		index = 0;
		dir1 = 0;
		dir2 = 0;
	}

	// Regression
//	double rxt, ryt;
//	double rx, ry, rt;
//	double rxx, ryy, rtt;
//	double rvx, rvy;
//	int n;
//	double r2x, r2y;
//	boolean regression;
	
	int dir1, dir2, state1, state2, index;
	byte s1, s2;
	
	public int move() {
		int iteration = 0;
		for(; iteration < Settings.itpf; iteration+=2) {
			changechunk: {
				if(y > cSIZEm) {
					y = 0;
					yc++;
					index -= cSIZE2;
				} else if(y < 0) {
					y = cSIZEm;
					yc--;
					index += cSIZE2;
				} else break changechunk;
				chunk = worker.getLevel().getChunk(xc, yc);
//				chunk = chunk.getNeighbour(xc, yc, dir2); //TODO fix memory leak
			}
		
			state1 = chunk.cells[index]++;
			if(chunk.cells[index] == rule.getSize()) chunk.cells[index] = 0;
			dir1 = (dir2 + rule.turn[state1])&0b11;
			index += directioni[dir1];
			x += directionx[dir1];

			changechunk: {
				if(x > cSIZEm) {
					x = 0;
					xc++;
					index -= cSIZE;
				} else if(x < 0) {
					x = cSIZEm;
					xc--;
					index += cSIZE;
				} else break changechunk;
				chunk = worker.getLevel().getChunk(xc, yc);
//				chunk = chunk.getNeighbour(xc, yc, dir1);
			}
			
			state2 = chunk.cells[index]++;
			if(chunk.cells[index] == rule.getSize()) chunk.cells[index] = 0;
			dir2 = (dir1 + rule.turn[state2])&0b11;
			index += directioni[dir2];
			y += directiony[dir2];
			
			if(findingPeriod()) {
				s1 = (byte)(dir1<<6 | state1);
				s2 = (byte)(dir2<<6 | state2);
				if(stateindex < states.length) {
					states[(int) stateindex++] = s1;
					states[(int) stateindex++] = s2;
				} else stateindex+=2;
				
				if(states[repeatLength]!=s1) {
					repeatLength = 0;
					minHighwayPeriod = stateindex;
					xend = getX();
					yend = getY();
				} else {
					repeatLength+=2;
					if(repeatLength == states.length || repeatLength > Settings.repeatcheck*minHighwayPeriod) {
						PERIODFOUND = true;
						saveState = false;
						break;
					}
				}
			}
		}
		return iteration;
	}
	
//	public void regression(long t) {
//		double x = this.x + (xc << Settings.cPOW);
//		double y = this.y + (yc << Settings.cPOW);
//
//		rxt += x*t;
//		ryt += y*t;
//		rx += x;
//		ry += y;
//		rt += t;
//		rxx += x*x;
//		ryy += y*y;
//		rtt += t*t;
//		n++;
//		rvx = (rxx-rx*rx/n)/n;
//		rvy = (ryy-ry*ry/n)/n;
//		
//		r2x = (n*rxt-rx*rt)*(n*rxt-rx*rt)/(double)((n*rxx-rx*rx)*(n*rtt-rt*rt));
//		r2y = (n*ryt-ry*rt)*(n*ryt-ry*rt)/(double)((n*ryy-ry*ry)*(n*rtt-rt*rt));
//	}
}