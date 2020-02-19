package com.camoga.ant;

import static com.camoga.ant.Settings.cSIZE;

import java.io.Serializable;
import java.util.ArrayList;

import com.camoga.ant.Rule.CellColor;

/**
 * y++: down
 * x++: right
 *
 */
public class Level {
	
	public static ArrayList<Chunk> chunks = new ArrayList<Chunk>();
	public static Chunk lastChunk;
	
	static class Chunk implements Serializable {
		int x, y;
		
		long lastVisit;
		
		public int[] cells = new int[cSIZE*cSIZE];
		
		public Chunk(int x, int y) {
			this.x = x;
			this.y = y;
			lastVisit = Simulation.iterations;
		}
	}
	
	public static void init() {
		chunks.clear();
		chunks = new ArrayList<Chunk>();
		chunks.add(new Chunk(0, 0));
		lastChunk = chunks.get(0);
	}
	
	/**
	 * 
	 * @param xc x coord of chunk
	 * @param yc y coord of chunk
	 * @param create chunk if doesn't exist
	 * @return
	 */
	public static Chunk getChunk(int xc, int yc, boolean create) {
		if(xc == lastChunk.x && yc == lastChunk.y) {
			lastChunk.lastVisit = Simulation.iterations;
			return lastChunk;
		}
		for(int i = chunks.size()-1; i >= 0; i--) {
			Chunk c = chunks.get(i);
			if(xc == c.x && yc == c.y) {
				lastChunk = c;
				lastChunk.lastVisit = Simulation.iterations;
//				Collections.swap(chunks, i, chunks.size()-1); // faster in some situations
				return c;
			}
		}
		if(!create) return null;
		Chunk c = new Chunk(xc, yc);
		chunks.add(c);
		if(!Ant.saveState && Settings.detectHighways && !Ant.CYCLEFOUND && Math.max(Math.abs(xc),Math.abs(yc)) > Settings.chunkCheck) {
			Ant.saveState = true;
			Ant.states[0] = (byte)(Ant.dir<<6 | Ant.state);
//			int state = c.cells[getCellIndex(Ant.x, Ant.y)];
//			int d = (Ant.dir + (Rule.colors[state].right ? 1:-1))&0b11;
//			Simulation.ant.xs = Ant.x+Ant.directions[d][0];
//			Simulation.ant.ys = Ant.y+Ant.directions[d][1];
		}
		return c;
	}
	
	public static Chunk getChunk2(int xc, int yc) {
		for(int i = chunks.size()-1; i >= 0; i--) {
			try {
				Chunk c = chunks.get(i);
				if(c==null) continue;
				if(xc == c.x && yc == c.y) {
					return c;
				}				
			} catch(IndexOutOfBoundsException e) {
				continue;
			}
		}
		return null;
	}

	//TODO improve render
	public static void render(int[] pixels, int chunks, int width, int height, boolean followAnt) {
		int xa = followAnt ? Ant.xc:0;
		int ya = followAnt ? Ant.yc:0;

		CellColor[] colors = Rule.colors;
		
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
					int y = yo+ycf;
					for(int xo = 0; xo < cSIZE; xo++) {
						int index = (xo+xcf) + y*width;
						if(index >= pixels.length) continue;
						pixels[index] = colors[c.cells[i]].color;
						i++;
					}
				}
			}
		}
		
		// Shear (really slow)
//		int xoffset = width;
//		for(int y = 0; y < height; y++) {
//			int yo = y+ya*cSIZE;
//			for(int x = 0; x < width; x++) {
//				int xo = x+xa*cSIZE;
//				int a = xo-xoffset;
//				int b = yo-x+width*2-height/2-xoffset;
//				pixels[x+y*width] = Rule.colors[getChunkByCoord(a, b).cells[getCellIndex(a, b)]].color;
////				System.out.println(xo + ", " +  yo +" : "+ (xo+yo));
//			}
//		}
//		
//		System.out.println("{"+Ant.x + ", " + Window.iterations+"}");
		
	}
}