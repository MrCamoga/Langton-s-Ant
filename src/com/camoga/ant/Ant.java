package com.camoga.ant;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.annotation.Repeatable;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import com.camoga.ant.Level.Chunk;

public class Ant {
	static int dir;
	static int xc,yc;
	static int x, y;
	
	static final int[][] directions = new int[][] {{0,-1},{1,0},{0,1},{-1,0}};
	
	public Ant(int x, int y) {
		Ant.x = x&Settings.cSIZEm;
		Ant.y = y&Settings.cSIZEm;
		Ant.xc = x>>Settings.cPOW;
		Ant.yc = y>>Settings.cPOW;
		Ant.dir = 0;
		try {
			raf = new RandomAccessFile(Settings.statefile, "rw");
			mbbr = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, Settings.fileChunkSize);
			mbbw = mbbr;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @return true if ant forms a highway
	 */
	public int move() {
		int i = 0;
		for(; i < Settings.itpf; i++) {
			Chunk c = Level.getChunk(xc, yc, true);
			int index = x|(y<<Settings.cPOW);
			int state = c.cells[index];
			boolean right = Rule.colors[state].right;
			if(checkCycle(dir, state)) break;
			dir = (dir + (right ? 1:-1))&0b11;
			c.cells[index] = (state+1) % Rule.colors.length;
			
			x += directions[dir][0];
			y += directions[dir][1];
			
			//OPTIMIZE (chunk coordinates can only change if x/y = 0/cSIZE)
			if(x > Settings.cSIZEm) {
				x = 0;
				xc++;
			} else if(x < 0) {
				x = Settings.cSIZEm;
				xc--;
			} else if(y > Settings.cSIZEm) {
				y = 0;
				yc++;
			} else if(y < 0) {
				y = Settings.cSIZEm;
				yc--;
			}
		}
		return i;
	}
	
	boolean saveState = false;
	private long currentCycleLength = 0;
	private long check = 0;
	private static MappedByteBuffer mbbr, mbbw; // mbbr: reading, mbbw: writing
	private static RandomAccessFile raf;
	private static int chunkLoadedr = 0, chunkLoadedw = 0;
	private long index = 0;
	
	long minHighwayPeriod = 0;  // This is the final cycle length
	boolean CYCLEFOUND = false;
	
	int xs = 0;
	int ys = 0;
	
	private boolean checkCycle(int dir, int state) {
		if(!saveState) return false;
		try {
			byte s1 = (byte)(dir<<6 | state); //Only works for rules with <= 64 colors
			if(index < 200000000) put(index, s1);
			index+=1;
			if(index > 1) {
				if(currentCycleLength == 0) {
					minHighwayPeriod = index-1;
				}
				
				byte s2 = get(currentCycleLength);
				if(s2==s1) currentCycleLength++;
				else currentCycleLength = 0;
				
				if(currentCycleLength >= minHighwayPeriod) {
					check++;
				}

				if(currentCycleLength == 200000000 || check > Settings.repeatcheck*minHighwayPeriod) {
					CYCLEFOUND = true;
					saveState = false;
//					System.out.println((x-xs-directions[dir][0]+1)/(Settings.repeatcheck+2)+ ", " + (y-ys-directions[dir][1]+1)/(Settings.repeatcheck+2));
					return true;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void put(long index, byte data) {
		int loc = (int)(index%Settings.fileChunkSize);
		int chunk = (int) (index/Settings.fileChunkSize);
		if(chunk >= Settings.maxNumOfChunks) throw new RuntimeException("Max capacity exceeded");
		if(chunk != chunkLoadedw) {
			chunkLoadedw = chunk;
			try {
				if(chunkLoadedr == chunkLoadedw) mbbw = mbbr;
				else mbbw = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, chunk*Settings.fileChunkSize, Settings.fileChunkSize);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		mbbw.put(loc, data);
	}
	
	private byte get(long index) {
		int loc = (int)(index%Settings.fileChunkSize);
		int chunk = (int) (index/Settings.fileChunkSize);
		if(chunk >= Settings.maxNumOfChunks) throw new RuntimeException("Max capacity exceeded");
		if(chunk != chunkLoadedr) {
			chunkLoadedr = chunk;
			try {
				if(chunkLoadedr == chunkLoadedw) mbbr = mbbw;
				else mbbr = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, chunk*Settings.fileChunkSize, Settings.fileChunkSize);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return mbbr.get(loc);
	}
}