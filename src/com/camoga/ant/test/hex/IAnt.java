package com.camoga.ant.test.hex;

public interface IAnt {
	public int move();
	
	public void init(long rule, long iterations);
	
	public long getPeriod();
	public int getXC();
	public int getYC();
	public IRule getRule();
	
	public boolean findingPeriod();
	
	public boolean periodFound();

	public void setFindingPeriod(boolean b);
	
	public void saveState();
	
	public void initPeriodFinding();
	
}