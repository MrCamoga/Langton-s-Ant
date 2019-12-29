package com.camoga.ant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

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
			mbb = new RandomAccessFile("uwu.uwu", "rw").getChannel().map(FileChannel.MapMode.READ_WRITE, 0, Settings.fileSize);
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
	
//	public boolean checkCycleOld(Direction dir, int state) {
//		if(!saveState) return false;
//		lastStates.add((byte)dir.id);
//		lastStates.add((byte)state);
//		if(lastStates.size() > 2) {
//			if(currentCycleLength == 0) {
//				minCycleLength = lastStates.size()/2-1;
//			}
//			if(lastStates.get(currentCycleLength*2) == (byte)dir.id && lastStates.get(currentCycleLength*2+1) == (byte)state) {
//				currentCycleLength++;
//			} else {
//				currentCycleLength = 0;
//			}
//			if(currentCycleLength >= minCycleLength) {
//				check++;
//			}
//			if(Math.random() > 0.9999) {
//				System.out.println(currentCycleLength + ", " + maxcycle + ", " + minCycleLength);
//			}
//			if(check > repeatcheck*minCycleLength) {
//				System.out.println(check + ", " + repeatcheck + "*" + minCycleLength);
//				CYCLEFOUND = true;
//				saveState = false;
//				getCycleStart();
//
//				return true;
//			}
//		}
//		return false;
//	}
	
	private int currentCycleLength = 0;
	long minCycleLength = 0;  // This is the final cycle length
	boolean CYCLEFOUND = false;
	private long check = 0;
//	ArrayList<Byte> lastStates = new ArrayList<>();
	private int repeatcheck = 1;
	public long highwaystart = 0;
	private MappedByteBuffer mbb;
	private int index = 0;
	private boolean checkCycle(Direction dir, int state) {
		try {
			if(!saveState) return false;
			mbb.put(index, (byte)dir.id);
			mbb.put(index+1, (byte)state);
			index+=2;
			if(index > 2) {
				if(currentCycleLength == 0) {
					minCycleLength = index/2-1;
				}
				
				if(mbb.get(2*currentCycleLength) == (byte)dir.id && mbb.get(2*currentCycleLength+1) == (byte)state) {
					currentCycleLength++;
				} else {
					currentCycleLength = 0;
				}
				if(currentCycleLength >= minCycleLength) {
					check++;
				}
//				if(Math.random() > 0.99) {
//					System.out.println(currentCycleLength + ", " + check + ", " + minCycleLength);
//				}
				if(check > repeatcheck*minCycleLength) {
					System.out.println(check + ", " + repeatcheck + "*" + minCycleLength);
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
	
	private void getCycleStart() throws Exception {
		int dx = 0;
		int dy = 0;
		
		for(int i = 0; i < minCycleLength; i++) {
			int dirr = mbb.get(2*i);
			Direction d = 	dirr == 0 ? Direction.NORTH:
							dirr == 1 ? Direction.EAST:
							dirr == 2 ? Direction.SOUTH:Direction.WEST;
			
			dx += d.dx;
			dy += d.dy;
		}
		int xr = x-dx;
		int yr = y-dy;
		int state = Level.getState(xr, yr);
		while(state == (state = Level.getState(xr -= dx, yr -= dy))) {}
		xr += dx;
		yr += dy;
		if(dx!=0) highwaystart = Window.iterations - ((x-xr)/dx-1)*minCycleLength; // upper bound, lower bound = highwaystart - minCycleLength
		else highwaystart = Window.iterations - ((y-yr)/dy-1)*minCycleLength;
		System.out.println("Size of highway: " + dx + "×" + dy + ", starts at: " + highwaystart);
//		System.out.println(Long.toHexString(Rule.colors.get(Level.getState(xr+dx, yr+dy)).color));
	}
}