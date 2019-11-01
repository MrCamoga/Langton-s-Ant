package com.camoga.ant;

import java.util.ArrayList;
import java.util.List;

/**
 * y++: down
 * x++: right
 *
 */
public class Level {
	
	public static List<Chunk> chunks;
	private static Chunk lastChunk; 
	static final int cSIZE = 64; // chunks are powers of two because bitwise op are faster
	
	static class Chunk {
		int x, y;
		
		public int[] cells = new int[cSIZE*cSIZE];
		
		public Chunk(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	
	public Level() {
		chunks = new ArrayList<Chunk>()	;
		lastChunk = null;
	}
	
	public static void updateState(int x, int y) {
		int xc = getChunkCoord(x);
		int yc = getChunkCoord(y);

		int xo = getCoord(x);
		int yo = getCoord(y);
		
		Chunk c = getChunk(xc, yc, true);
		c.cells[xo+yo*cSIZE] += 1;
		c.cells[xo+yo*cSIZE] %= Rule.colors.size();
	}
	
	public static boolean isRight(int x, int y) {
		return Rule.colors.get(getState(x,y)).right;
	}
	
	private static int getChunkCoord(int p) {
		return (int)Math.floor(p/(float)cSIZE);
	}
	
	private static int getCoord(int p) {
		return p&(cSIZE-1);
	}
	
	public static int getState(int x, int y) {
		return getChunkByCoord(x, y).cells[getCellIndex(x,y)];
	}
	
	private static int getCellIndex(int x, int y) {
		return getCoord(x)+getCoord(y)*cSIZE;
	}
	
	public static Chunk getChunkByCoord(int x, int y) {
		return getChunk(getChunkCoord(x), getChunkCoord(y), true);
	}
	
	/**
	 * 
	 * @param xc x coord of chunk
	 * @param yc y coord of chunk
	 * @param create chunk if doesn't exist
	 * @return
	 */
	public static Chunk getChunk(int xc, int yc, boolean create) {
//		HashMap<Integer, Chunk> ca = chunks.get(xc);
//		if(ca != null) {
//			Chunk c = ca.get(yc);
//			if(c != null) return c;
//			c = new Chunk(xc, yc);
//			ca.put(yc, c);
//			return c;
//		}
//		ca = new HashMap<Integer, Chunk>();
//		chunks.put(xc, ca);
//		Chunk c = new Chunk(xc, yc);
//		ca.put(yc, c);
//		
//		return c;
		if(lastChunk != null) {
			if(xc == lastChunk.x && yc == lastChunk.y) return lastChunk;
		}
		for(int i = chunks.size()-1; i >= 0; i--) {
			Chunk c = chunks.get(i);
			if(xc == c.x && yc == c.y) {
				lastChunk = c;
				return c;
			}
		}
		if(!create) return null;
		chunks.add(new Chunk(xc, yc));
		return chunks.get(chunks.size()-1);
	}

	//TODO improve render
	public static void render(int[] pixels, int chunks) {
		for(int yc = 0; yc < chunks; yc++) {
			int ycf = yc*cSIZE;
			for(int xc = 0; xc < chunks; xc++) {
				Chunk c = Level.getChunk(xc-chunks/2, yc-chunks/2, false);
				if(c == null) continue;
				int xcf = xc*cSIZE;
				int i = 0;
				for(int yo = 0; yo < cSIZE; yo++) {
					for(int xo = 0; xo < cSIZE; xo++) {
						int index = (xo+xcf) + (yo+ycf)*Window.image.getWidth();
						if(index >= pixels.length) continue;
						pixels[index] = Rule.colors.get(c.cells[i]).color;
						i++;
					}
				}
			}
		}
		
	}
}