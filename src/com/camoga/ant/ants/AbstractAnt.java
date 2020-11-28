package com.camoga.ant.ants;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map.Entry;

import org.apache.commons.collections4.keyvalue.MultiKey;

import com.camoga.ant.Settings;
import com.camoga.ant.Worker;
import com.camoga.ant.level.Level.Chunk;

public abstract class AbstractAnt {

	protected Worker worker;
	protected AbstractRule rule;
	
	protected Chunk chunk;
	protected int state = 0;
	
	protected int dir;
	protected int xc,yc;
	protected int x, y;
	
	protected byte[] states;
	protected boolean saveState = false;
	protected int repeatLength = 0;
	protected long stateindex = 1;
	public long xstart, ystart, xend, yend;
	
	protected long minHighwayPeriod = 0;  // This is the final period length
	protected boolean PERIODFOUND = false;
	
	public AbstractAnt(Worker worker) {
		this.worker = worker;
	}
	
	public abstract int move();
	
	public void init(long rule, long iterations) {
		int stateslen = iterations == -1 ? 200000000:(int) Math.min(Math.max(5000000,iterations/(int)Settings.repeatcheck*2), 200000000);
		if(states == null || states.length != stateslen) states = new byte[stateslen];
		this.rule.createRule(rule);
		x = 0;
		y = 0;
		xc = 0;
		yc = 0;		
		dir = 5;
		state = 0;
		saveState = false;
		repeatLength = 1;
		states[1] = -1;
		stateindex = 0;
		minHighwayPeriod = 0;
		PERIODFOUND = false;
		chunk = worker.getLevel().chunks.get(0,0);
	}
	
	public long getPeriod() {return minHighwayPeriod;}
	public int getX() {
		return x + xc*Settings.cSIZE;
	}
	public int getY() {
		return y + yc*Settings.cSIZE;
	}
	public int getXC() {return xc;}
	public int getYC() {return yc;}
	public AbstractRule getRule() {return rule;}
	
	public boolean findingPeriod() {return saveState;}
	
	public boolean periodFound() {return PERIODFOUND;}
	
	public void setFindingPeriod(boolean b) {
		saveState = b;
		xstart = getX();
		ystart = getY();
	}
	
	public void saveState(String file) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeLong(rule.getRule());
			oos.writeLong(worker.getIterations());
			oos.writeInt(dir);
			oos.writeInt(state);
			oos.writeInt(x);
			oos.writeInt(y);
			oos.writeInt(xc);
			oos.writeInt(yc);
			oos.writeBoolean(saveState);
			if(saveState) {
				oos.writeLong(stateindex);
				oos.writeInt(repeatLength);
				oos.writeLong(minHighwayPeriod);
				oos.write(states);
			}
			oos.writeByte(Settings.cPOW);
			oos.writeInt(worker.getLevel().chunks.size());
			for(Entry<MultiKey<? extends Integer>, Chunk> c : worker.getLevel().chunks.entrySet()) {
				MultiKey<? extends Integer> key = c.getKey();
				oos.writeInt(key.getKey(0));
				oos.writeInt(key.getKey(1));
				oos.write(c.getValue().cells);
			}
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}