package com.camoga.ant.ants;

import java.util.ArrayList;
import java.util.Arrays;

import com.camoga.ant.Settings;
import com.camoga.ant.ants.Map.Chunk;
import com.camoga.ant.ants.patterns.Pattern;
import static com.camoga.ant.Main.LOG;

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

	private long[] histogram;
	private long[] matchResets = new long[1000000];
	private ArrayList<Integer> matchResets2 = new ArrayList<Integer>();
	
	public void init(long rule, long iterations) {
		super.init(rule, iterations);
		histogram = new long[65535];
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
			state = chunk.cells[index]++;
			if(resetState && chunk.cells[index] == rule.getSize()) chunk.cells[index] = 0;
			dir = transform[(rule.turn[state]<<5)|dir];
			
			x += directionx[dir];
			y += directiony[dir];
			z += directionz[dir];
			
			if(saveState) {
				short s1 = (short) ((state << 5) | dir);	
				if(states[match]!=s1) {
					if(period > 27542350 && period < 27542400) {
						LOG.info(match + ", " + period);
					}
					if(match < matchResets.length) matchResets[match]++;
					else matchResets2.add(match);
					histogram[state]++;
					match = 0;
					period = stateindex;
					xend = getX();
					yend = getY();
					zend = getZ();
				} else {
					match++;
					if(match == states.length || match > 200000+Settings.repeatpercent*period) {
						PERIODFOUND = true;
						saveState = false;
						resetState = true;
						break;
					}
				}

				if(stateindex < states.length) {
					states[(int) stateindex] = s1;
				}
				stateindex++;
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

	public ResultSet run(long rule, long maxiterations, Pattern pattern) {
		init(rule, maxiterations, pattern);
		int maxChunk = 1;
		boolean extended = false;
		long resetIterations = 0;
		
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

			if(!resetState && !saveState && iterations - resetIterations > 30000000) {
				setFindingPeriod(true);
			}

			// Detect highways
			if(resetState && !findingPeriod() && !periodFound()) {
				//Farthest chunk ant has traveled
				int maxc = Math.max(Math.abs(getZC()), Math.max(Math.abs(getXC()), Math.abs(getYC())));
				if(maxc > maxChunk) maxChunk = maxc;
				if(maxChunk > Settings.chunkCheck && map.chunks.size() < 0.2*maxChunk*maxChunk*maxChunk) { // Proportion of chunks generated over size of square that bounds all chunks. If prop -> 0 ant forms a highway (prop might go near 0 if ant forms a thin triangle)
					map.deleteOldChunks = true;
					resetState = false;
					resetIterations = iterations;
				}
			}
			
			// if((getType() > 0 || (getType()==0 && (rule & (rule+1))==0)) && !extended && map.chunks.size() <= 4 &&  maxiterations != -1 && getIterations() > maxiterations) {
			if((!resetState || saveState) && !extended) {
				extended = true;
				maxiterations += 60_000_000L;
				// setFindingPeriod(true);
			}
			// }
		}

		computeHistogram();
		// LOG.info(maxChunk+", " + saveState + ", " + resetState + ", " + extended + ", " + getPeriod());
		return getResult();
	}

	private ResultSet getResult() {
		long period = periodFound() ? getPeriod():(findingPeriod() ? 1:0);
		int hash = computeHash();
		long[] d = {Math.abs(xend-xstart), Math.abs(yend-ystart), Math.abs(zend-zstart)}; 
		// if(period > 1) { // detect anomalies
		// 	if(isTriangle(d)) period = 0;
		// }
		if(period <= 1) return new ResultSet(rule,iterations,hash,period);
		Arrays.sort(d);
		LOG.info(rule + ", " + iterations + ", " + hash + ", " + period + ", " + d[2] + ", " + d[1] + ", " + d[0] + ", " + Arrays.toString(Arrays.copyOf(histogram,30)));
		return new ResultSet(rule,iterations,hash,period,d[2],d[1],d[0],histogram);
	}


	private void computeHistogram() {
		int histogramFinished = 0;
		for(int m : matchResets2) {
			for(int i = 0; i < m; i++) {
				histogram[states[i]]++;
			}
		}
		for(int i = 0; i < matchResets.length; i++) {
			if(matchResets[i] == 0) continue;
			for(int j = 0; j < i; j++) {
				histogramFinished += matchResets[i];
				histogram[states[j]] += matchResets[i];
			}
			if(histogramFinished == period) break;
		}
	}
	
	public long getPeriod() {
		return period + 1;
	}
}