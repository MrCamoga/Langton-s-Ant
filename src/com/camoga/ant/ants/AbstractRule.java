package com.camoga.ant.ants;

public abstract class AbstractRule {
	
	protected long rule;
	public int[] turn;
	protected int size;
	protected int[] colors;
	
	public abstract void createRule(long rule);
	
	public abstract String string();

	public long getRule() {return rule;}
	public int[] getColors() {return colors;}
	
	public int getSize() {return size;}
}