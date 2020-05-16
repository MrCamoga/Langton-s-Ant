package com.camoga.ant;

import static com.camoga.ant.Settings.cSIZE;

import java.io.Serializable;

import org.apache.commons.collections4.map.MultiKeyMap;

import com.camoga.ant.Rule.CellColor;

/**
 * y++: down
 * x++: right
 *
 */
public class Level {
	
	public static MultiKeyMap<Integer,Chunk> chunks = new MultiKeyMap<Integer,Chunk>();
	
	static class Chunk implements Serializable {		
		long lastVisit;
		
		public byte[] cells = new byte[cSIZE*cSIZE];
		
		public Chunk() {
			lastVisit = Simulation.iterations;
		}
	}
	
	public static void init() {
		chunks.clear();
		chunks = new MultiKeyMap<Integer,Chunk>();
		chunks.put(0,0,new Chunk());
	}
	
	/**
	 * 
	 * @param xc x coord of chunk
	 * @param yc y coord of chunk
	 * @return
	 */
	public static Chunk getChunk(int xc, int yc) {
		Chunk result = chunks.get(xc,yc);
		if(result != null) {
			result.lastVisit = Simulation.iterations;
			return result;
		}

		Chunk c = new Chunk();
		chunks.put(xc,yc,c);
		if(!Ant.saveState && !Ant.CYCLEFOUND && Math.max(Math.abs(xc),Math.abs(yc)) > Settings.chunkCheck) {
			Ant.saveState = true;
			Ant.states[0] = (byte)(Ant.dir<<6 | Ant.state);
		}
		return c;
	}
	
	public static Chunk getChunk2(int xc, int yc) {
		return chunks.get(xc,yc);
	}

	//TODO improve render
	public static void render(int[] pixels, int chunks, int width, int height, boolean followAnt) {
		int xa = followAnt ? Ant.xc:0;
		int ya = followAnt ? Ant.yc:0;
		
//		System.out.println(xa+","+ya);

		CellColor[] colors = Rule.colors;
		if(colors == null || colors[0] == null) return;
		
		if(!Settings.renderVoid) for(int i = 0; i < pixels.length; i++) {
			pixels[i] = colors[0].color;
		} 
		else for(int i = 0; i < pixels.length; i++) {
			pixels[i] = 0xff000000;
		}
		
		for(int yc = 0; yc < chunks; yc++) {
			int ycf = yc<<Settings.cPOW;
			for(int xc = 0; xc < chunks; xc++) {
				Chunk c = getChunk2(xc-chunks/2+xa, yc-chunks/2+ya);
				if(c == null) continue;
				int xcf = xc<<Settings.cPOW;
				int i = 0;
				for(int yo = 0; yo < cSIZE; yo++) {
					int y = (yo|ycf) * width;
					for(int xo = 0; xo < cSIZE; xo++) {
						int index = (xo|xcf) + y;
						if(index >= pixels.length) continue;
						pixels[index] = colors[c.cells[i]%Rule.size].color;
						i++;
					}
				}
			}
		}
		
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
//						pixels[xp+yp*width] = colors[c.cells[xo+yo*cSIZE]].color;
//					}
//				}
//			}
//		}
//		System.out.println(Ant.x|(Ant.xc<<Settings.cPOW));
//		Settings.highwaySizew = 1000000;
//		Settings.highwaySizeh = 768;
//		Settings.deleteOldChunks = true;
//		Settings.toot = true; 
//		System.out.println(Settings.highwaySizeh+","+Settings.highwaySizew);
//		Settings.itpf = 100;
//		System.out.println("{"+(Ant.x|(Ant.xc<<Settings.cPOW)) + ", " + Simulation.iterations+"}");
	}
	
	public static void renderHighway(byte[] pixels, int chunks, int width, int height, boolean followAnt) {
		int xa = followAnt ? Ant.xc:0;
		int ya = followAnt ? Ant.yc:0;
//		int ya = -xa+1;
		int offset = 7;
		
		//Shear transformation		
		for(int xc = -(Settings.highwaySizew>>Settings.cPOW); xc < 10; xc++) {
//			System.out.println(xc);
			int xcf = (xc+(Settings.highwaySizew>>Settings.cPOW))<<Settings.cPOW;
			for(int yc = -1; yc < 25; yc++) {
				int ycf = (yc-offset)<<Settings.cPOW;
				Chunk c = getChunk2(xc+xa-9, yc+ya-xc);
				if(c==null) continue;
				for(int yo = 0; yo < cSIZE; yo++) {
					int y = yo|ycf;
					for(int xo = 0; xo < cSIZE; xo++) {
						int xp = (xo|xcf);
						int yp = y+xo;
						if(xp < 0 || yp < 0 || yp >= height || xp >= width) continue;
						pixels[xp+yp*width] = (byte) c.cells[xo+yo*cSIZE];
					}
				}
			}
		}
//		System.out.println(Ant.x|(Ant.xc<<Settings.cPOW));
//		
//		System.out.println("{"+(Ant.x|(Ant.xc<<Settings.cPOW)) + ", " + Simulation.iterations+"}");
		
	}
}