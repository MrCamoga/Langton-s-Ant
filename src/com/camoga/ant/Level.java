package com.camoga.ant;

import java.util.ArrayList;
import java.util.List;

import static com.camoga.ant.Settings.cSIZE;

/**
 * y++: down
 * x++: right
 *
 */
public class Level {
	
	public static List<Chunk> chunks;
	private static Chunk lastChunk; 
	
	static class Chunk {
		int x, y;
		
		long lastVisit;
		
		public int[] cells = new int[cSIZE*cSIZE];
		
		public Chunk(int x, int y) {
			this.x = x;
			this.y = y;
			lastVisit = Window.iterations;
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
		int i = getCellIndex(xo, yo);
		c.cells[i] = (c.cells[i]+1) % Rule.colors.size();
	}
	
	public static boolean isRight(int x, int y) {
		return Rule.colors.get(getState(x,y)).right;
	}
	
	private static int getChunkCoord(int p) {
		return p>>Settings.cPOW;
	}
	
	private static int getCoord(int p) {
		return p&(Settings.cSIZEm);
	}
	
	public static int getState(int x, int y) {
		return getChunkByCoord(x, y).cells[getCellIndex(x,y)];
	}
	
	private static int getCellIndex(int x, int y) {
		return getCoord(x)|(getCoord(y)<<Settings.cPOW);
	}
	
	public static Chunk getChunkByCoord(int x, int y) {
		int xc = getChunkCoord(x);
		int yc = getChunkCoord(y);
		return getChunk(xc, yc, true);
	}
	
	/**
	 * 
	 * @param xc x coord of chunk
	 * @param yc y coord of chunk
	 * @param create chunk if doesn't exist
	 * @return
	 */
	public static Chunk getChunk(int xc, int yc, boolean create) {
		if(lastChunk != null) {
			if(xc == lastChunk.x && yc == lastChunk.y) {
				lastChunk.lastVisit = Window.iterations;
				return lastChunk;
			}
		}
		for(int i = chunks.size()-1; i >= 0; i--) {
			Chunk c = chunks.get(i);
			if(xc == c.x && yc == c.y) {
				lastChunk = c;
				lastChunk.lastVisit = Window.iterations;
				return c;
			}
		}
		if(!create) return null;
		chunks.add(new Chunk(xc, yc));

		if(Math.max(Math.abs(xc),Math.abs(yc)) > Settings.chunkCheck && !Window.ant.CYCLEFOUND && Settings.detectCycles) {
			Window.ant.saveState = true;
		}
		return chunks.get(chunks.size()-1);
	}

	//TODO improve render
	public static void render(int[] pixels, int chunks, int width, int height) {
		int xa = Settings.followAnt ? getChunkCoord(Ant.x):0;
		int ya = Settings.followAnt ? getChunkCoord(Ant.y):0;

		if(!Settings.renderVoid) for(int i = 0; i < pixels.length; i++) {
			pixels[i] = Rule.colors.get(0).color;
		} 
		else for(int i = 0; i < pixels.length; i++) {
			pixels[i] = 0xff000000;
		}
		
		for(int yc = 0; yc < chunks; yc++) {
			int ycf = yc<<Settings.cPOW;
			for(int xc = 0; xc < chunks; xc++) {
				Chunk c = Level.getChunk(xc-chunks/2+xa, yc-chunks/2+ya, false);
				if(c == null) continue;
				int xcf = xc<<Settings.cPOW;
				int i = 0;
				for(int yo = 0; yo < cSIZE; yo++) {
					int y = yo+ycf;
					for(int xo = 0; xo < cSIZE; xo++) {
						int index = (xo+xcf) + y*Window.canvasImage.getWidth();
						if(index >= pixels.length) continue;
						pixels[index] = Rule.colors.get(c.cells[i]).color;
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
//				pixels[x+y*width] = Rule.colors.get(getState(xo-xoffset, yo+x-height/2-xoffset)).color;
////				System.out.println(xo + ", " +  yo +" : "+ (xo+yo));
//			}
//		}
//		
//		System.out.println("{"+Ant.x + ", " + Window.iterations+"}");
		
	}
}