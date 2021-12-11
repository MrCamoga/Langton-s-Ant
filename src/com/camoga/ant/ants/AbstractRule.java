package com.camoga.ant.ants;

public abstract class AbstractRule {
	
	protected long rule;
	public int[] turn;
	public int[] letter;
	protected int size;
	protected int[] colors;
	protected String ruleString;
	
	public abstract void createRule(long rule);

	/**
	 * Returns rule string currently being simulated
	 * @return
	 */
	public String toString() {return ruleString;}

	public long getRule() {return rule;}
	public int[] getColors() {return colors;}
	
	public int getSize() {return size;}
}