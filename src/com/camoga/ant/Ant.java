package com.camoga.ant;

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
	}
	
	boolean saveState = false;
	
	public void move() {
		int state = Level.getState(x, y);
		boolean right = Rule.colors.get(state).right;
		if(checkCycle(dir, state)) return;
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
	}
	
	private int currentCycleLength = 0;
	int minCycleLength = 0;  // This is the final cycle length
	boolean CYCLEFOUND = false;
	private int check = 0;
	ArrayList<Byte> lastStates = new ArrayList<>();
	private int repeatcheck = 5;
	private int maxcycle = 0;
	public boolean checkCycle(Direction dir, int state) {
		if(!saveState) return false;
		lastStates.add((byte)dir.id);
		lastStates.add((byte)state);
		if(lastStates.size() > 2) {
			if(currentCycleLength == 0) {
				minCycleLength = lastStates.size()/2-1;
			}
			if(lastStates.get(currentCycleLength*2) == (byte)dir.id && lastStates.get(currentCycleLength*2+1) == (byte)state) {
				currentCycleLength++;
			} else {
				currentCycleLength = 0;
			}
			if(currentCycleLength >= minCycleLength) {
				check++;
			}
			if(check > repeatcheck*minCycleLength) {
				CYCLEFOUND = true;
				saveState = false;
//				getCycleStart();

				return true;
			}
		}
		return false;
	}
	
	private void getCycleStart() {
		int dx = 0;
		int dy = 0;
		
		for(int i = 0; i < minCycleLength; i++) {
			int dirr = lastStates.get(2*i);
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
//		System.out.println(dx + ", " + dy);
//		System.out.println((xr+dx) + "," + (yr+dy));
	}
}