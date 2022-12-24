package com.camoga.ant.level;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import org.apache.commons.collections4.map.MultiKeyMap;

import com.camoga.ant.Settings;
import com.camoga.ant.Worker;
import com.camoga.ant.ants.AbstractAnt;

/**
 * y++: down
 * x++: right
 *
 */
public class Level {
	
	public MultiKeyMap<Integer, Chunk> chunks = new MultiKeyMap<Integer, Chunk>();
	public boolean deleteOldChunks = false;
	
	public int chunkSize;
	
	public class Chunk implements Serializable {		
		public long lastVisit;
		
		public byte[] cells = new byte[chunkSize];
		
//		public Chunk[] neighbours = new Chunk[4];
		
		/**
		 * Returns neighbour chunk in direction dir, creates one if doesn't exist and checks for highways
		 * @param xc x coordinate of neighbour chunk
		 * @param yc y coordinate of neighbour chunk
		 * @param dir direction of neighbour
		 * @return neighbour chunk
		 */
//		public Chunk getNeighbour(int xc, int yc, int dir) {
//			if(neighbours[dir] == null) {
//				neighbours[dir] = Level.this.getChunk(xc, yc);
//				neighbours[dir].neighbours[dir^2] = this;
//			}
//			neighbours[dir].lastVisit = worker.getIterations();
//			return neighbours[dir];
//		}
	}
	
	private Worker worker;
	
	public Level(Worker worker) {
		this.worker = worker;
	}
	
	public void init() {
		chunks.clear();
		deleteOldChunks = false;
		maxChunk = 1;
	}
	
	public int maxChunk;
	
	/**
	 * Get chunk at coordinate xc, yc and creates one if doesn't exist.
	 * @param xc x coord of chunk
	 * @param yc y coord of chunk
	 * @return
	 */
	public Chunk getChunk(int xc, int yc) {
		Chunk result = chunks.get(xc,yc);
		if(result == null) chunks.put(xc,yc,result = new Chunk());
		result.lastVisit = worker.getIterations();
		
		return result;
	}
	
	/**
	 * 
	 * @param xc x coord of chunk
	 * @param yc y coord of chunk
	 * @param zc z coord of chunk
	 * @return
	 */
	public Chunk getChunk(int xc, int yc, int zc) {
		Chunk result = chunks.get(xc,yc,zc);
		if(result == null) chunks.put(xc,yc,zc,result = new Chunk());
		result.lastVisit = worker.getIterations();
		
		return result;
	}

	public Chunk getChunk(int xc, int yc, int zc, int wc) {
		Chunk result = chunks.get(xc,yc,zc,wc);
		if(result == null) chunks.put(xc,yc,zc,wc,result = new Chunk());
		result.lastVisit = worker.getIterations();
		
		return result;
	}
	
	public Chunk getChunk2(int xc, int yc) { return chunks.get(xc,yc); }
	
	public Chunk getChunk2(int xc, int yc, int zc) { return chunks.get(xc,yc,zc); }
	
	public Chunk getChunk2(int xc, int yc, int zc, int wc) { return chunks.get(xc,yc,zc,wc); }

	
	private static Font font = new Font("Tahoma", Font.PLAIN, 12);
	//TODO improve render
	public void render(BufferedImage image, int[] pixels, int width, int height, boolean followAnt, boolean info) {
		int xa = followAnt ? worker.getAnt().getXC():0;
		int ya = followAnt ? worker.getAnt().getYC():0;
		int za = followAnt ? worker.getAnt().getZC():0;
		
		Graphics g = image.getGraphics();
//		g.setFont(font);
		AbstractAnt ant = worker.getAnt();
		
//		System.out.println(xa+","+ya);

		int[] colors = ant.getRule().getColors();
		if(colors == null) return;
		
		if(!Settings.renderVoid) for(int i = 0; i < pixels.length; i++) {
			pixels[i] = colors[0];
		}
		else for(int i = 0; i < pixels.length; i++) {
			pixels[i] = 0xff000000;
		}
		
		int cSIZE = ant.cSIZE;
		int cPOW = ant.cPOW;

		int xchunks = width/cSIZE;
		int ychunks = height/cSIZE;
		int zchunks = 1024/cSIZE;

		if(worker.getType() <= 1) {
			for(int yc = 0; yc < ychunks; yc++) {
				int ycf = yc<<cPOW;
				for(int xc = 0; xc < xchunks; xc++) {
					Chunk c = getChunk2(xc-xchunks/2+xa, yc-ychunks/2+ya);
					if(c == null) continue;
					int xcf = xc<<cPOW;
					int i = 0;
					for(int yo = 0; yo < cSIZE; yo++) {
						int y = (yo|ycf)*width;
						for(int xo = 0; xo < cSIZE; xo++) {
							int index = (xo|xcf) + y;
							if(index >= pixels.length) continue;
							pixels[index] = colors[c.cells[i]%colors.length];
							i++;
						}
					}
				}
			}			
		} else if(worker.getType() == 2) { // z-axis projection
			for(int zc = -2; zc <= 2; zc++) {
//				int zcf = zc<<(cPOW*2);
				for(int yc = 0; yc < ychunks; yc++) {
					int ycf = yc<<cPOW;
					for(int xc = 0; xc < xchunks; xc++) {
						Chunk c = getChunk2(xc-xchunks/2+xa, yc-ychunks/2+ya, za + zc);
						if(c == null) continue;
						int xcf = xc<<cPOW;
						int i = 0;
						for(int yo = 0; yo < cSIZE; yo++) {
							int y = (yo|ycf)*width;
							for(int xo = 0; xo < cSIZE; xo++) {
								int index = (xo|xcf) + y;
								if(index >= pixels.length) {
									i++;
									continue;
								}
								for(int zo = 0; zo < c.cells.length; zo += 1 <<(cPOW*2)) {
									int state = c.cells[zo|i];
									if(state == 0) continue;
									pixels[index] = colors[state%colors.length];
									break;
								}
								i++;
							}
						}							
					}
				}
			}
		}

		int h = 5;
		int gap = 16;
		g.setColor(Color.WHITE);
		g.drawString(String.format("Iterations: %,d", worker.getIterations()), 10, h+=gap); 
		g.drawString(String.format("Rule: %s (%s)", ant.getRule(), Long.toUnsignedString(ant.getRule().getRule())), 10, h+=gap);
		
		if(ant.findingPeriod()) {
			g.setColor(Color.red);
			g.drawString(String.format("Finding period... %,d", ant.getPeriod()), 10, h+=gap);
		} else if(ant.periodFound()) {
			g.setColor(Color.WHITE);
			g.drawString(String.format("Period: %,d", ant.getPeriod()), 10, h+=gap);
		}
		//HEXAGONAL GRID
//		else if(worker.getType()==1) {
//			Chunk c = getChunk2(0, 0);
//			if(c == null) return;
//			for(int x = 0; x < cSIZE; x++) {
//				int xp = (int) (000 + 5.77*x);
//				for(int y = 0; y < cSIZE; y++) {
//					int yp = 400 - 10*y - x*5;
//					for(int yh = 0; yh < 7; yh++) {
//						int yf = yp+yh+y;
//						for(int xh = 0; xh < 7; xh++) {
//							int xf = xp+xh+x;
//							if(xf < 0 || yf < 0 || xf >= width || yf >= height) continue;
//							pixels[xf+yf*width] = colors[c.cells[x|(y<<Settings.cPOW)]%colors.length];
//						}
//					}
//				}
//			}
//		}
		
		// SKEW TRANSFORMATION TO STRAIGHTEN HIGHWAYS
//		for(int xc = -(Settings.highwaySizew/128>>Settings.cPOW); xc < 10; xc++) {
//			int xcf = (xc+(Settings.highwaySizew/128>>Settings.cPOW))<<Settings.cPOW;
//			for(int yc = -1; yc < 25; yc++) {
//				int ycf = (yc-7)<<Settings.cPOW;
//				Chunk c = getChunk2(xc+xa-9, yc+ya-xc);
//				if(c==null) {
//					for(int yo = 0; yo < cSIZE; yo++) {
//						int y = yo|ycf;
//						for(int xo = 0; xo < cSIZE; xo++) {
//							int xp = (xo|xcf);
//							int yp = y+xo;
//							if(xp < 0 || yp < 0 || yp >= height || xp >= width) continue;
//							pixels[xp+yp*width] = 0xffff0000;
//						}
//					}
//					continue;
//				};
//				for(int yo = 0; yo < cSIZE; yo++) {
//					int y = yo|ycf;
//					for(int xo = 0; xo < cSIZE; xo++) {
//						int xp = (xo|xcf);
//						int yp = y+xo;
//						if(xp < 0 || yp < 0 || yp >= height || xp >= width) continue;
//						pixels[xp+yp*width] = colors[c.cells[xo+yo*cSIZE]];
//					}
//				}
//			}
//		}
	}
}