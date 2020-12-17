package com.camoga.ant.ants;

import com.camoga.ant.Settings;
import com.camoga.ant.Worker;

public class Ant extends AbstractAnt {

	static final int[] directionx = new int[] {0,1,0,-1};
	static final int[] directiony = new int[] {-1,0,1,0};
	
	public Ant(Worker worker) {
		super(worker,2);
		rule = new Rule();
	}
	
	public void init(long rule, long iterations) {
		super.init(rule, iterations);
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
	}

	// Regression
//	double rxt, ryt;
//	double rx, ry, rt;
//	double rxx, ryy, rtt;
//	double rvx, rvy;
//	int n;
//	double r2x, r2y;
//	boolean regression;
	
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
		
			int index = x|(y<<cPOW);
			state = chunk.cells[index];
			dir = (dir + rule.turn[state])&0b11;
			if(++chunk.cells[index] == rule.getSize()) chunk.cells[index] = 0;
			
			x += directionx[dir];
			
			if(findingPeriod()) {
				byte s = (byte)(dir<<6 | state);
				if(stateindex < states.length) states[(int) stateindex] = s;
				stateindex++;
				if(states[repeatLength]!=s) {
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
			
			index = x|(y<<cPOW);
			state = chunk.cells[index];
			dir = (dir + rule.turn[state])&0b11;
			if(++chunk.cells[index] == rule.getSize()) chunk.cells[index] = 0;
			
			y += directiony[dir];
			
			if(findingPeriod()) {
				byte s = (byte)(dir<<6 | state);
				if(stateindex < states.length) states[(int) stateindex] = s;
				stateindex++;
				
				if(states[repeatLength]!=s) {
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