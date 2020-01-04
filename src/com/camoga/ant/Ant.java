package com.camoga.ant;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Ant {
	static Direction dir;
	static int x, y;

	private static int count = 0;
	enum Direction {
		NORTH(0,-1),EAST(1,0),SOUTH(0,1),WEST(-1,0);
		
		int dx, dy;
		int id;
		
		Direction(int dx, int dy) {
			id = count;
			count++;
			this.dx = dx;
			this.dy = dy;
		}
		
		public int getX() {
			return dx;
		}
		
		public int getY() {
			return dy;
		}
	}
	
	public Ant(int x, int y) {
		Ant.x = x;
		Ant.y = y;
		Ant.dir = Direction.NORTH;
		try {
			raf = new RandomAccessFile("langton.buf", "rw");
			 mbbr = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, Settings.fileSize);
			 mbbw = mbbr;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	boolean saveState = false;
	
	/**
	 * 
	 * @return true if ant forms a highway
	 */
	public boolean move() {
		int state = Level.getState(x, y);
		boolean right = Rule.colors.get(state).right;
		if(checkCycle(dir, state)) return true;
		switch(dir) {
		case NORTH:
			dir = right ? Direction.EAST:Direction.WEST;
			break;
		case EAST:
			dir = right ? Direction.SOUTH:Direction.NORTH;
			break;
		case SOUTH:
			dir = right ? Direction.WEST:Direction.EAST;
			break;
		case WEST:
			dir = right ? Direction.NORTH:Direction.SOUTH;
			break;
		}
		Level.updateState(x, y);
		
		x += dir.getX();
		y += dir.getY();
		return false;
	}
	
	private long currentCycleLength = 0;
	private long check = 0;
	private static MappedByteBuffer mbbr, mbbw; // mbbr: reading, mbbw: writing
	private static RandomAccessFile raf;
	private static int chunkLoadedr = 0, chunkLoadedw = 0;
	private long index = 0;
	
	long minCycleLength = 0;  // This is the final cycle length
	boolean CYCLEFOUND = false;
	
	private boolean checkCycle(Direction dir, int state) {
		try {
			if(!saveState) return false;
			put(index, (byte)(dir.id<<6 | state)); //Only works for rules with <= 64 colors
			index+=1;
			if(index > 1) {
				if(currentCycleLength == 0) {
					minCycleLength = index-1;
				}
				
				int s = get(currentCycleLength)&0xff;
				if(s>>6 == (byte)dir.id && (s&0b111111) == (byte)state) {
					currentCycleLength++;
				} else {
					currentCycleLength = 0;
				}
				if(currentCycleLength >= minCycleLength) {
					check++;
				}
				if(check > Settings.repeatcheck*minCycleLength) {
					CYCLEFOUND = true;
					saveState = false;
					//TODO calculate highway size here
					return true;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void put(long index, byte data) {
		int loc = (int)(index%Settings.fileSize);
		int chunk = (int) (index/Settings.fileSize);
		if(chunk > Settings.maxNumOfChunks) throw new RuntimeException("Max capacity exceeded");
		if(chunk != chunkLoadedw) {
			chunkLoadedw = chunk;
			try {
				if(chunkLoadedr == chunkLoadedw) mbbw = mbbr;
				else mbbw = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, chunk*Settings.fileSize, Settings.fileSize);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		mbbw.put(loc, data);
	}
	
	private byte get(long index) {
		int loc = (int)(index%Settings.fileSize);
		int chunk = (int) (index/Settings.fileSize);
		if(chunk > Settings.maxNumOfChunks) throw new RuntimeException("Max capacity exceeded");
		if(chunk != chunkLoadedr) {
			chunkLoadedr = chunk;
			try {
				if(chunkLoadedr == chunkLoadedw) mbbr = mbbw;
				else mbbr = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, chunk*Settings.fileSize, Settings.fileSize);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return mbbr.get(loc);
	}
}