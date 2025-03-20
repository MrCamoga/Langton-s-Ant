package com.camoga.ant.ants;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import com.camoga.ant.Settings;
import com.camoga.ant.Worker;
import com.camoga.ant.ants.Map.Chunk;

public class Ant4D extends AbstractAnt {

//	import numpy as np
//	from itertools import product
//	points = [v for v in product([-1,1],repeat=4)]
//	S = Symmetric(16)
//	R = np.matrix([[0,-1,0,0],[1,0,0,0],[0,0,1,0],[0,0,0,1]]) # zw-plane rotation
//	[points.index(tuple(R.dot(p).tolist()[0])) for p in points] # permutation on 16 points
//	[8, 9, 10, 11, 0, 1, 2, 3, 12, 13, 14, 15, 4, 5, 6, 7]
//	H = Subgroup(S,gens={11212469476416,5579497751056,2621655963493})
//	RL,UD,XY = (100, 99, 97)
//	def e1(g): # Image of vector e1
//		return np.add(points[H[g][8]],points[H[g][15]])/2
//	direction = array([e1(g) for g in range(len(H))]).transpose()
//	transform = cayleyTable(H)
	
	static final int[] directionx = new int[] {1,0,0,0,1,0,1,0,0,0,0,0,0,1,0,1,0,0,0,1,0,0,0,0,1,0,0,0,1,0,1,0,0,0,0,0,0,1,0,1,0,0,0,1,0,0,0,0,0,1,0,1,0,0,0,1,0,0,0,0,1,0,0,0,1,0,1,0,0,0,0,0,0,1,0,1,0,0,0,1,0,0,0,0,1,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,-1,0,-1,0,0,0,-1,0,0,0,0,-1,0,0,0,-1,0,-1,0,0,0,0,0,0,-1,0,-1,0,0,0,-1,0,0,0,0,-1,0,0,0,-1,0,-1,0,0,0,0,0,-1,0,0,0,-1,0,-1,0,0,0,0,0,0,-1,0,-1,0,0,0,-1,0,0,0,0,-1,0,0,0,-1,0,-1,0,0,0,0,0,0,-1,0,-1,0,0,0,-1};
	static final int[] directiony = new int[] {0,0,1,1,0,0,0,0,0,0,1,0,1,0,0,0,0,1,0,0,0,1,0,0,0,0,1,1,0,0,0,0,0,0,1,0,1,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,-1,0,0,0,-1,0,-1,0,0,0,0,0,0,-1,0,-1,0,0,0,-1,0,0,0,0,-1,0,0,0,-1,0,-1,0,0,0,0,0,0,-1,0,-1,0,0,0,-1,1,0,0,0,1,0,1,0,0,0,0,0,0,1,0,1,0,0,0,1,0,0,0,0,1,0,0,0,1,0,1,0,0,0,0,0,0,1,0,1,0,0,0,1,0,0,0,0,0,0,-1,0,0,0,-1,0,0,0,0,-1,0,-1,0,0,0,0,0,0,-1,-1,0,0,0,0,-1,0,0,0,-1,0,0,0,0,-1,0,-1,0,0,0,0,0,0,-1,-1,0,0};
	static final int[] directionz = new int[] {0,1,0,0,0,0,0,0,1,1,0,0,0,0,1,0,0,0,1,0,0,0,0,1,0,0,0,0,0,-1,0,-1,0,0,0,-1,0,0,0,0,-1,0,0,0,-1,0,-1,0,1,0,0,0,0,1,0,0,0,1,0,0,0,0,1,1,0,0,0,0,0,0,1,0,0,0,-1,0,0,0,-1,0,0,0,0,-1,0,-1,0,0,0,0,0,0,-1,-1,0,0,0,0,1,1,0,0,0,0,0,0,1,0,1,0,0,0,0,1,0,0,0,1,0,0,0,-1,0,0,0,0,0,0,-1,-1,0,0,0,0,-1,0,0,0,-1,0,0,0,0,-1,0,1,0,1,0,0,0,1,0,0,0,0,1,0,0,0,1,0,1,0,0,0,0,0,-1,0,0,0,0,-1,0,0,0,-1,0,0,0,0,-1,-1,0,0,0,0,0,0,-1,0};
	static final int[] directionw = new int[] {0,0,0,0,0,1,0,1,0,0,0,1,0,0,0,0,-1,0,0,0,-1,0,-1,0,0,1,0,0,0,0,0,0,1,1,0,0,0,0,-1,0,0,0,-1,0,0,0,0,-1,0,0,1,0,0,0,1,0,0,0,0,1,0,-1,0,0,0,0,0,0,-1,-1,0,0,1,0,0,0,0,1,0,0,0,1,0,0,0,0,-1,-1,0,0,0,0,0,0,-1,0,0,1,0,0,0,0,0,0,1,1,0,0,0,0,-1,0,0,0,-1,0,0,0,0,-1,0,0,1,1,0,0,0,0,0,0,1,0,-1,0,0,0,0,-1,0,0,0,-1,0,0,1,0,0,0,0,1,0,0,0,1,0,0,0,0,-1,-1,0,0,0,0,0,0,-1,0,0,1,0,1,0,0,0,1,0,0,0,0,-1,0,0,0,-1,0,-1,0,0,0,0,0};
	
	// Cayley table for the symmetry group of the 4-cube
	static int[] transform;
	
	static {
		transform = new int[192*256];
		try {
			DataInputStream dis = new DataInputStream(Ant4D.class.getClassLoader().getResourceAsStream("4d.table"));
			for(int i = 0; dis.available() > 0;) {
				if((i&0xff) == 192) i+=64;
				transform[i++] = dis.read()&0xff;
			}
			dis.close();
			if(Arrays.hashCode(transform) != 447971329) throw new RuntimeException("Invalid 4d table.");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public Ant4D() {
		super(3,4);
		rule = new Rule4D();
	}
	
	public void init(long rule, long iterations) {
		super.init(rule, iterations);
		match = 2;
		states[2] = -1;
	}

	public void move() {
		int iteration = 0;
		for(; iteration < Settings.itpf; iteration++) {
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
				} else if(w > cSIZEm) {
					w = 0;
					wc++;
				} else if(w < 0) {
					w = cSIZEm;
					wc--;
				} else break changechunk;
				chunk = map.getChunk(xc, yc, zc, wc);
			}
			
			int index = (((((w<<cPOW)|z)<<cPOW)|y)<<cPOW)|x;
			state = chunk.cells[index];

			dir = transform[(dir<<8) | rule.turn[state]];
			if(++chunk.cells[index] == rule.getSize()) chunk.cells[index] = 0;
			
			x += directionx[dir];
			y += directiony[dir];
			z += directionz[dir];
			w += directionw[dir];
			
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
					wend = getW();
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
		for(int h = -2, wc = this.wc, w = this.w+h; h < 3; h++, w++) {
			if(w < 0) {
				w += cSIZE;
				wc--;
			} else if(w > cSIZEm) {
				w-= cSIZE;
				wc++;
			}
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
						Chunk c = map.getChunk2(xc, yc, zc, wc);
						hash = 31*hash + (c!=null ? c.cells[(((((w<<cPOW)|z)<<cPOW)|y)<<cPOW)|x]:0);
					}
				}
			}
		}
		return hash;
	}
	
	public long getPeriod() {
		return period/2;
	}
}