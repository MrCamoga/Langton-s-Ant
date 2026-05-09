package com.camoga.ant.ants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import com.camoga.ant.Main;
import com.camoga.ant.Settings;
import com.camoga.ant.ants.Map.Chunk;
import com.camoga.ant.ants.patterns.Pattern;

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
		histogram = new long[65535];
		// histogram2 = new long[65535];
	}
	
	public int dirx, diry, state1, state2, index;
	short s1, s2;
	private long[] histogram;
	// private long[] histogram2;
	public int[] matchResets = new int[100000];
	public int matchResetIndex = 0;
	public HashMap<Integer,Integer> matchResets2 = new HashMap<Integer,Integer>();
	
	public void move(long it) {
		int iteration = 0;
		int e;
		int mask = -(1<<cPOW);
		for(; iteration < it; iteration+=2) {
			if((e=y&mask) != 0) {
				yc += e>>cPOW;
				y -= e;
				chunk = chunk.getNeighbour(xc, yc, diry+2);
			}
		
			index = (y<<cPOW)|x;
			state1 = chunk.cells[index]++;
			if(resetState && chunk.cells[index] == rule.getSize()) chunk.cells[index] = 0;
			dirx = -diry*rule.turn[state1];
			x += dirx;
			if((e=x&mask) != 0) {
				xc += e >> cPOW;
				x -= e;
				chunk = chunk.getNeighbour(xc, yc, dirx+1);
			}

			index = (y<<cPOW)|x;
			state2 = chunk.cells[index]++;
			if(resetState && chunk.cells[index] == rule.getSize()) chunk.cells[index] = 0;
			diry = dirx*rule.turn[state2];
			y += diry;
			
			if(saveState) {
				s1 = (short) state1;
				s2 = (short) state2;
				if(stateindex < states.length) {
					states[(int) stateindex++] = s1;
					states[(int) stateindex++] = s2;
				} else stateindex+=2;

				if(states[match] != s1 || states[match+1] != s2) {
					matchResets[match]++;
					// matchResets2.compute(match, (k,v) -> v==null ? 1:v+1);
					histogram[s1]++;
					histogram[s2]++;
					match = 0;
					period = stateindex;
					xend = getX();
					yend = getY();
					// for(int i = 0; i <= maxstate; i++) {
					// 	histogram[i] += histogram2[i];
					//  	histogram2[i] = 0;
					// }
				} else {
					match += 2;
					if(match == states.length || match > 200000+Settings.repeatpercent*period) {
						PERIODFOUND = true;
						saveState = false;
						resetState = true;
						break;
					}
				}
			}
		}
		iterations += (long)iteration;
	}

	public ResultSet run(long rule, long maxiterations, Pattern pattern) {
		init(rule, maxiterations, pattern);
		int maxChunk = 1;
		long autosavetimer = System.currentTimeMillis();
		int initialDir = 1;
		boolean extended = false;
		
		while(!periodFound() && (maxiterations == -1 || getIterations() < maxiterations)) {
			move(Settings.itpf);
			if(stateindex > 2000000 && stateindex <= 2000000+Settings.itpf) {
				int diry = initialDir, dirx = 0;
				long x = xstart, y = ystart;
				long[] histogram = new long[65535];
				histogram[states[0]]++;
				histogram[states[1]]++;
				dirx = -diry*this.rule.turn[states[0]];
				x += dirx;
				diry = dirx*this.rule.turn[states[1]];
				y += diry;
				outer: for(int p = 2; p < 1000000; p+=2) {
					for(int i = 0; i < 2000000-p; i+=2) {
						if(states[i] != states[i+p] || states[i+1] != states[i+1+p]) {
							histogram[states[p]]++;
							histogram[states[p+1]]++;
							dirx = -diry*this.rule.turn[states[p]];
							x += dirx;
							diry = dirx*this.rule.turn[states[p+1]];
							y += diry;
							continue outer;
						}
					}
					PERIODFOUND = true;
					saveState = false;
					resetState = true;
					System.arraycopy(histogram, 0, this.histogram, 0, histogram.length);
					period = p;
					xend = x;
					yend = y;
					long[] d = {Math.abs(xend-xstart), Math.abs(yend-ystart)}; 
					if(isTriangle(d)) System.exit(0);
					return getResult();
				}
			}
			
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
			if(!resetState && !saveState) { // Change this so that it takes a specific number of iterations (1-4M) instead of one loop
				setFindingPeriod(true);
				initialDir = diry;
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
				Main.LOG.info("Autosaving " + getRule());
				autosavetimer = System.currentTimeMillis();
			}
			
			// if((getType() > 0 || (getType()==0 && (rule & (rule+1))==0)) && !extended && map.chunks.size() <= 4 &&  maxiterations != -1 && getIterations() > maxiterations) {
			if(saveState && !extended) {
				extended = true;
				maxiterations += 10_000_000L;
				// setFindingPeriod(true);
			}
			// }
		}

		// for(Entry<Integer,Integer> entry : matchResets2.entrySet()) {
		// 	for(int i = 0; i < entry.getKey(); i++) {
		// 		histogram[states[i]] += entry.getValue();
		// 	}
		// }

		for(int i = 0; i < matchResets.length; i++) {
			if(matchResets[i] == 0) continue;
			for(int j = 0; j < i; j++) {
				histogram[states[j]] += matchResets[i];
			}
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
			// if((winding&3) != 0) period = 1;
		}
		winding >>= 2;
		if(period <= 1) return new ResultSet(rule,iterations,hash,period);
		Arrays.sort(d);
		return new ResultSet(rule,iterations,hash,period,d[1],d[0],winding,histogram);
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
