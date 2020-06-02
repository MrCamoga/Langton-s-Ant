package com.camoga.ant.test.hex;

public interface IRule {
	
	public void createRule(long rule);
	
	public String string();

	public long getRule();
	public int[] getColors();
	
	public int getSize();
	public int get(int index);
}
