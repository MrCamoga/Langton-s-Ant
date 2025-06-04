package com.camoga.ant.ants;

import java.util.Arrays;
import java.util.Scanner;

import com.camoga.ant.Settings;
import com.camoga.ant.ants.Map.Chunk;
import com.camoga.ant.net.Client;

public class Ant extends AbstractAnt {

	public Ant() {
		super(0,2);
		rule = new Rule();
	}
	
	public void init(long rule, long iterations) {
		super.init(rule, iterations);
		index = 0;
		diry=1;
		dirx=0;
		directionend = 0;
		histogram = new long[255];
		histogram2 = new long[255];
	}
	
	public int dirx, diry, state1, state2, index;
	short s1, s2;
	public long[] histogram;
	long[] histogram2;
	
	public void move(long it) {
		int iteration = 0;
		int e;
		int mask = -(1<<cPOW);
		boolean first = true;
		for(; iteration < it; iteration+=2) {
			if((e=y&mask) != 0) {
				yc += e>>cPOW;
				y -= e;
				chunk = chunk.getNeighbour(xc, yc, diry+2);
			}
			/* changechunk: {
				if(y > cSIZEm) {
					y = 0;
					yc++;
				} else if(y < 0) {
					y = cSIZEm;
					yc--;
				} else break changechunk;
				chunk = chunk.getNeighbour(xc, yc, diry+2);
			} */
		
			index = (y<<cPOW)|x;
			state1 = (chunk.cells[index]++) & 0xff;
			if(resetState && chunk.cells[index] == rule.getSize()) chunk.cells[index] = 0;
			// if(!resetState && state1 > maxstate) maxstate = state1;
			// direction += rule.turn[state1];
			dirx = -diry*rule.turn[state1];
			x += dirx;

			/* changechunk: {
				if(x > cSIZEm) {
					x = 0;
					xc++;
				} else if(x < 0) {
					x = cSIZEm;
					xc--;
				} else break changechunk;
				chunk = chunk.getNeighbour(xc, yc, dirx+1);
			} */
			if((e=x&mask) != 0) {
				xc += e >> cPOW;
				x -= e;
				chunk = chunk.getNeighbour(xc, yc, dirx+1);
			}

			index = (y<<cPOW)|x;
			state2 = (chunk.cells[index]++) & 0xff;
			if(resetState && chunk.cells[index] == rule.getSize()) chunk.cells[index] = 0;
			// if(!resetState && state2 > maxstate) maxstate = state2;
			// direction += rule.turn[state2];
			diry = dirx*rule.turn[state2];
			y += diry;
			
			if(saveState) {
				/*if(stateindex == 0 && first) { // code to only start period when high state is reached. high sstates are less common and less likely to cause a period skip
					if (state1 == 16 || state2 == 16) {
						xstart = getX();
						ystart = getY();
						directionstart = direction;
						first = false;
					}
					continue;
				}*/
				s1 = (short)(state1^dirx);
				s2 = (short)(state2^diry);
				if(stateindex < states.length) {
					states[(int) stateindex++] = s1;
					states[(int) stateindex++] = s2;
				} else stateindex+=2;

				// histogram2[state1]++;
				// histogram2[state2]++;

				if(states[match] != s1 || states[match+1] != s2) {
					match = 0;
					period = stateindex;
					xend = getX();
					yend = getY();
					// directionend = direction;
					// for(int i = 0; i <= 254; i++) {
					// 	histogram[i] += histogram2[i];
					// 	histogram2[i] = 0;
					// }
				} else {
					match += 2;
					if(match == states.length || match > 200000+Settings.repeatpercent*period) {
						PERIODFOUND = true;
						saveState = false;
						resetState = true;
						for(int i = 0; i < period; i++) {
							short st = states[i];
							st ^= st < 0 ? -1:1;
							directionend += rule.turn[st];
							histogram[st]++;
						}
						break;
					}
				}
			}
		}
		iterations += (long)iteration;
	}

	public ResultSet run(long rule, long maxiterations) {
		init(rule, maxiterations);
		int maxChunk = 1;
		long autosavetimer = System.currentTimeMillis();
		
		while(!periodFound() && (maxiterations == -1 || getIterations() < maxiterations)) {
			move(Settings.itpf);

			// Chunk deletion only activates once highway computation has started
			if(map.deleteOldChunks && getIterations() > 1000000000) {
				map.chunks.entrySet().removeIf(e -> {
					for(int i = 0; i < dimension; i++) {
						if(Math.abs(e.getKey().getKey(i)-getChunkCoord(i)) > 200) {
							e.getValue().destroy();
							return true;	// if ant is far away from chunk, delete. // TODO take into account chunk size
						}
					}
					return false;
				});
			}

			// before period calculation starts, resetState is set to false, and after 1M iterations, period calculation starts, this allows for the states to stabilize after resetting
			if(!resetState && !saveState) {
				setFindingPeriod(true);
			}

			// Detect highways
			if(!findingPeriod() && !periodFound()) {
				//Farthest chunk ant has traveled
				int maxc = Math.max(Math.abs(getXC()), Math.abs(getYC()));
				if(maxc > maxChunk) maxChunk = maxc;
				if(maxChunk > Settings.chunkCheck && map.chunks.size() < 0.2*maxChunk*maxChunk) { // Proportion of chunks generated over size of square that bounds all chunks. If prop -> 0 ant forms a highway (prop might go near 0 if ant forms a thin triangle)
					map.deleteOldChunks = true;
					resetState = false;
				}
			}
			
			if(Settings.autosave && maxiterations > 50e9 && System.currentTimeMillis()-autosavetimer > 900000) { // Autosave every 15 mins
				saveState(getRule()+".state");
				Client.LOG.info("Autosaving " + getRule());
				autosavetimer = System.currentTimeMillis();
			}
			
			// if((getType() > 0 || (getType()==0 && (rule & (rule+1))==0)) && !extended && map.chunks.size() <= 4 &&  maxiterations != -1 && getIterations() > maxiterations) {
			// 	extended = true;
			// 	maxiterations += 100000000;
			// 	setFindingPeriod(true);
			// }
		}

		return getResult();
	}

	private ResultSet getResult() {
		long period = periodFound() ? getPeriod():(findingPeriod() ? 1:0);
		long winding = (directionend-directionstart);
		int hash = computeHash();
		long[] d = {Math.abs(xend-xstart), Math.abs(yend-ystart)};
		if(period > 1) { // detect anomalies
			if(isTriangle(d)) period = 0;
			if((winding&3) != 0) period = 1;
		}
		winding >>= 2;
		if(period <= 1) return new ResultSet(rule,iterations,hash,period);
		Arrays.sort(d);
		return new ResultSet(rule,iterations,hash,period,d[0],d[1],winding,histogram);
	}

	/**
	 * Computes the angle between the ant position from the origin and the direction of the highway.
	 * If a period is detected on the side of the triangle, the direction of the "highway" wouldn't be parallel to the ant
	 * @param highwaysize 
	 * @return
	 */
	private boolean isTriangle(long[] highwaysize) {
		long x = getX(), y = getY();
		double antdist2 = x*x+y*y;
		double highsize2 = highwaysize[0]*highwaysize[0]+highwaysize[1]*highwaysize[1];
		double dot = (getX()*(xend-xstart)+getY()*(yend-ystart))/Math.sqrt(antdist2*highsize2);
		return dot < 0.3;
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
				Chunk c = map.getChunk2(xc, yc);
				hash = 31*hash + (c!=null ? c.cells[(y<<cPOW) | x]:0);
			}
		}
		return hash;
	}
}
