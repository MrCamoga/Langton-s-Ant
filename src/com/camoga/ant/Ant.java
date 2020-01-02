package com.camoga.ant;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

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
			RandomAccessFile raf = new RandomAccessFile("langton.buf", "rw");
			for(int i = 0; i < Settings.numOfFiles; i++) {
				mbbs.add(raf.getChannel().map(FileChannel.MapMode.READ_WRITE, i*Settings.fileSize, Settings.fileSize));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	boolean saveState = false;
	
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
	private ArrayList<MappedByteBuffer> mbbs = new ArrayList<>();
	private long index = 0;
	
	long minCycleLength = 0;  // This is the final cycle length
	boolean CYCLEFOUND = false;
	
	private boolean checkCycle(Direction dir, int state) {
		try {
			if(!saveState) return false;
			mbbs.get((int) (index/Settings.fileSize)).put((int) (index%Settings.fileSize), (byte)(dir.id<<6 | state)); //Only works for rules with <= 64 colors
			index+=1;
			if(index > 1) {
				if(currentCycleLength == 0) {
					minCycleLength = index-1;
				}
				
				int s = mbbs.get((int) (currentCycleLength/Settings.fileSize)).get((int) (currentCycleLength%Settings.fileSize))&0xff;
				if(s>>6 == (byte)dir.id && (s&0b111111) == (byte)state) {
					currentCycleLength++;
				} else {
					currentCycleLength = 0;
				}
				if(currentCycleLength >= minCycleLength) {
					check++;
				}
//				if(Math.random() > 0.999993) {
//					System.out.println(currentCycleLength + ", " + check + ", " + minCycleLength);
//				}
				if(check > Settings.repeatcheck*minCycleLength) {
					CYCLEFOUND = true;
					saveState = false;
					getCycleStart();
					
					return true;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void getCycleStart() throws Exception {
		int dx = 0;
		int dy = 0;
		for(long i = 0; i < minCycleLength; i++) {
			int dirr = (mbbs.get((int) (i/Settings.fileSize)).get((int) (i%Settings.fileSize))&0xff) >> 6;
			Direction d = 	dirr == 0 ? Direction.NORTH:
							dirr == 1 ? Direction.EAST:
							dirr == 2 ? Direction.SOUTH:Direction.WEST;
			
			dx += d.dx;
			dy += d.dy;
		}
//		int xr = x;
//		int yr = y;
//		int state = Level.getState(xr, yr);
//		while(state == (state = Level.getState(xr -= dx, yr -= dy))) {
//			System.out.println(xr + ", " + yr);
//		}
//		xr += dx;
//		yr += dy;
//		if(dx!=0) highwaystart = Window.iterations - ((x-xr)/dx)*minCycleLength; // upper bound, lower bound = highwaystart - minCycleLength
//		else highwaystart = Window.iterations - ((y-yr)/dy)*minCycleLength;
		System.out.println("Size of highway: " + dx + "×" + dy);
	}
}