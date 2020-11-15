package com.camoga.ant.level;

import static com.camoga.ant.Settings.cSIZE;

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
	
	public class Chunk implements Serializable {		
		public long lastVisit;
		
		public byte[] cells = new byte[cSIZE*cSIZE];
		
		public Chunk() {
			lastVisit = worker.getIterations();
		}
	}
	
	private Worker worker;
	
	public Level(Worker worker) {
		this.worker = worker;
	}
	
	public void init() {
		chunks.clear();
		chunks.put(0,0,new Chunk());
		deleteOldChunks = false;
		mc = 1;
		prop = 1;
	}
	
	int mc;
	public double prop;
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

		Chunk c = new Chunk();
		chunks.put(xc,yc,c);

		//Farthest chunk ant has traveled
		if(Math.abs(xc) > mc) mc = Math.abs(Math.max(xc,yc));

//		prop = chunks.size()/(double)(mc*mc); // Proportion of chunks generated over size of square that bounds all chunks. If prop -> 0 ant forms a highway (prop might go near 0 if ant forms a thin triangle)

//		if(!worker.getAnt().findingPeriod() && !worker.getAnt().periodFound() && mc > Settings.chunkCheck && prop < 0.05) {
		if(!worker.getAnt().findingPeriod() && !worker.getAnt().periodFound() && mc > Settings.chunkCheck) {
			worker.getAnt().setFindingPeriod(true);
			deleteOldChunks = true;
		}
		return c;
	}
	
	public Chunk getChunk2(int xc, int yc) {
		return chunks.get(xc,yc);
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
				int ycf = yc<<Settings.cPOW;
				for(int xc = 0; xc < chunks; xc++) {
					Chunk c = getChunk2(xc-chunks/2+xa, yc-chunks/2+ya);
					if(c == null) continue;
					int xcf = xc<<Settings.cPOW;
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