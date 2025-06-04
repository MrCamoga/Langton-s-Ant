package com.camoga.ant.ants;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map.Entry;

import org.apache.commons.collections4.keyvalue.MultiKey;

import com.camoga.ant.Settings;
import com.camoga.ant.ants.Map.Chunk;
import com.camoga.ant.ants.patterns.Pattern;

public abstract class AbstractAnt {

	protected AbstractRule rule;
	protected int type;
	
	public Chunk chunk;

	// Space/chunk
	public int dimension;
	public int cPOW;
	public int cSIZE;
	public int cSIZEm;
	public int cSIZE2;
	public Map map;
	
	// Ant
	public int dir;
	protected int state;
	public int wc,xc,yc,zc;
	public int w,x,y,z;
	protected long iterations;
	protected long maxiterations;
	
	// Highway
	public boolean saveState = false;
	public boolean resetState = true;
	public short[] states;
	protected int match;
	protected long stateindex;
	public long wstart, xstart, ystart, zstart, wend, xend, yend, zend;
	public long direction, directionstart, directionend;
	public int maxstate;
	protected long period = 0;  // This is the final period length
	protected boolean PERIODFOUND = false;
	
	/**
	 * 
	 * @param type unique identifier for the type of ant (0 square 2d, 1 hex, 2 3d, 3 4d, 4 45º, etc)
	 * @param dimension dimension of the grid
	 */
	public AbstractAnt(int type, int dimension) {
		this.dimension = dimension;
		this.type = type;
		this.map = new Map(this);
		if(dimension==2) cPOW = 7;
		else if(dimension==3) cPOW = 5;
		else if(dimension==4) cPOW = 4;
		cSIZE = 1<<cPOW;
		cSIZEm = cSIZE-1;
		cSIZE2 = cSIZE<<cPOW;
		map.chunkSize = 1<<(cPOW*dimension);
	}
	
	public abstract void move(long it);
	
	public ResultSet run(long rule, long maxiterations) {
		throw new RuntimeException("Not implemented");
	}
	/**
	 * Compute a hash around the ant for verification purposes.
	 * @return
	 */
	public abstract int computeHash();
	
	public void init(long rule, long maxiterations) {
		this.maxiterations = maxiterations;
		int stateslen = maxiterations == -1 ? 200000000:(int) Math.min(Math.max(5000000,maxiterations/(int)Settings.repeatpercent*2), 200000000);
		if(states == null || states.length != stateslen) states = new short[stateslen];
		this.rule.createRule(rule);
		map.init();
		iterations = 0;
		w = 0;
		x = 0;
		y = 0;
		z = 0;
		wc = 0;
		xc = 0;
		yc = 0;	
		zc = 0;
		dir = 0;
		direction = 0;
		maxstate = 0;
		state = 0;
		saveState = false;
		resetState = true;
		match = 2;
		states[1] = -1;
		stateindex = 0;
		period = 0;
		PERIODFOUND = false;
		if(dimension == 2) {
			chunk = map.getChunk(0, 0);
		} else if(dimension == 3) {
			chunk = map.getChunk(0, 0, 0);
		} else if(dimension == 4) {
			chunk = map.getChunk(0, 0, 0, 0);
		} else throw new RuntimeException("Invalid dimension");
	}

	public void init(long rule, long maxiterations, Pattern pattern) {
		this.init(rule, maxiterations);
		// TODO pass rule or something to the pattern to verify that the pattern doesnt have more states than the rule
		pattern.buildPattern(this);
	}
	
	public long getPeriod() { return period; }
	public long getW() { return w + wc*cSIZE; }
	public long getX() { return x + xc*cSIZE; }
	public long getY() { return y + yc*cSIZE; }
	public long getZ() { return z + zc*cSIZE; }
	public int getWC() { return wc; }
	public int getXC() { return xc; }
	public int getYC() { return yc; }
	public int getZC() { return zc; }
	public int getChunkCoord(int index) {
		return new int[] {xc,yc,zc,wc}[index];
	}
	public AbstractRule getRule() {return rule;}
	public int getType() { return type; };
	public long getIterations() { return iterations; }
	
	public boolean findingPeriod() {return saveState;}
	
	public boolean periodFound() {return PERIODFOUND;}
	
	public void setFindingPeriod(boolean b) {
		saveState = b;
		xstart = getX();
		ystart = getY();
		zstart = getZ();
		wstart = getW();
		directionstart = direction;
	}
	
	public void saveState(String file) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeLong(rule.getRule());
			oos.writeLong(iterations);
			oos.writeInt(dir);
			oos.writeInt(state);
			oos.writeInt(x);
			oos.writeInt(y);
			oos.writeInt(xc);
			oos.writeInt(yc);
			oos.writeBoolean(saveState);
			if(saveState) {
				oos.writeLong(stateindex);
				oos.writeInt(match);
				oos.writeLong(period);
				// oos.write(states);
			}
			oos.writeByte(cPOW);
			oos.writeInt(map.chunks.size());
			for(Entry<MultiKey<? extends Integer>, Chunk> c : map.chunks.entrySet()) {
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