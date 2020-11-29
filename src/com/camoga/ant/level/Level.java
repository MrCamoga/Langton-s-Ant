package com.camoga.ant.level;

import java.io.Serializable;

import org.apache.commons.collections4.map.MultiKeyMap;

import com.camoga.ant.Settings;
import com.camoga.ant.Worker;

/**
 * y++: down
 * x++: right
 *
 */
public class Level {
	
	public MultiKeyMap<Integer, Chunk> chunks = new MultiKeyMap<Integer, Chunk>();
	public boolean deleteOldChunks = false;
	
	private int chunkSize;
	public int dimension;
	public int cPOW;
	public int cSIZE;
	public int cSIZEm;
	
	public class Chunk implements Serializable {		
		public long lastVisit;
		
		public byte[] cells = new byte[chunkSize];
		
		public Chunk() {
			lastVisit = worker.getIterations();
		}
	}
	
	private Worker worker;
	
	public Level(Worker worker, int dimension) {
		this.worker = worker;
		this.dimension = dimension;
		cPOW = dimension == 2 ? 7:6;
		cSIZE = 1<<cPOW;
		cSIZEm = cSIZE-1;
		chunkSize = 1<<(cPOW*dimension);
	}
	
	public void init() {
		chunks.clear();
		deleteOldChunks = false;
		maxChunk = 1;
	}
	
	int maxChunk;
	/**
	 * 
	 * @param xc x coord of chunk
	 * @param yc y coord of chunk
	 * @return
	 */
	public Chunk getChunk(int xc, int yc) {
		Chunk result = chunks.get(xc,yc);
		if(result != null) {
			result.lastVisit = worker.getIterations();
			return result;
		}

		result = new Chunk();
		chunks.put(xc,yc,result);

		//Farthest chunk ant has traveled
		int max = Math.max(Math.abs(xc), Math.abs(yc));
		if(max > maxChunk) maxChunk = max;

		if(!worker.getAnt().findingPeriod() && !worker.getAnt().periodFound() && maxChunk > Settings.chunkCheck) {
			if(chunks.size()/(double)(maxChunk*maxChunk) < 0.20) { // Proportion of chunks generated over size of square that bounds all chunks. If prop -> 0 ant forms a highway (prop might go near 0 if ant forms a thin triangle)
				worker.getAnt().setFindingPeriod(true);
				deleteOldChunks = true;				
			}
		}
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
		if(result != null) {
			result.lastVisit = worker.getIterations();
			return result;
		}

		result = new Chunk();
		chunks.put(xc,yc,zc,result);

		//Farthest chunk ant has traveled
		int max = Math.max(Math.max(Math.abs(xc), Math.abs(yc)), Math.abs(zc));
		if(max > maxChunk) maxChunk = max;

		if(!worker.getAnt().findingPeriod() && !worker.getAnt().periodFound() && maxChunk > Settings.chunkCheck) {
			if(chunks.size()/(double)(maxChunk*maxChunk*maxChunk) < 0.20) { // Proportion of chunks generated over size of square that bounds all chunks. If prop -> 0 ant forms a highway (prop might go near 0 if ant forms a thin triangle)
				worker.getAnt().setFindingPeriod(true);
				deleteOldChunks = true;				
			}
		}
		return result;
	}
	
	public Chunk getChunk2(int xc, int yc) {
		return chunks.get(xc,yc);
	}
	
	public Chunk getChunk2(int xc, int yc, int zc) {
		return chunks.get(xc,yc,zc);
	}

	//TODO improve render
	public void render(int[] pixels, int chunks, int width, int height, boolean followAnt) {
		int xa = followAnt ? worker.getAnt().getXC():0;
		int ya = followAnt ? worker.getAnt().getYC():0;
		
//		System.out.println(xa+","+ya);

		int[] colors = worker.getAnt().getRule().getColors();
		if(colors == null) return;
		
		if(!Settings.renderVoid) for(int i = 0; i < pixels.length; i++) {
			pixels[i] = colors[0];
		}
		else for(int i = 0; i < pixels.length; i++) {
			pixels[i] = 0xff000000;
		}

//		if(worker.getType() == 0) {
			for(int yc = 0; yc < chunks; yc++) {
				int ycf = yc<<cPOW;
				for(int xc = 0; xc < chunks; xc++) {
					Chunk c = getChunk2(xc-chunks/2+xa, yc-chunks/2+ya);
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
//		} //HEXAGONAL GRID
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