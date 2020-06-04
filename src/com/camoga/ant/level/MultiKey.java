package com.camoga.ant.level;

public class MultiKey {
	
	private int hashCode;
	private int x,y;
	
	public MultiKey(int x, int y) {
		this.x = x;
		this.y = y;
		hashCode = x ^ y;
	}
	
	public boolean equals(final Object obj) {
		if(obj == this) return true;
		if(obj instanceof MultiKey) {
			MultiKey otherKey = (MultiKey) obj;
			return otherKey.getX() == x && otherKey.getY() == y;
		}
		return false;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int hashCode() {
		return hashCode;
	}
}
