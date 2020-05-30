package com.camoga.ant.test.hex;

import com.camoga.ant.Level.Chunk;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map.Entry;

import org.apache.commons.collections4.keyvalue.MultiKey;

import com.camoga.ant.Settings;
import com.camoga.ant.Worker;

public class HexAnt implements IAnt {
	int dir;
	int xc,yc;
	int x, y;

	Chunk chunk;
	int state = 0;
	
	static final int[][] directions = new int[][] {{-1,-1},{0,-1},{1,0},{1,1},{0,1},{-1,0}};
	
	private byte[] states;
	private boolean saveState = false;
	private int repeatLength = 0;
	private long index = 1;
	
	private long minHighwayPeriod = 0;  // This is the final period length
	private boolean PERIODFOUND = false;
	
	private Worker worker;
	private HexRule rule;
	
	public HexAnt(Worker worker) {
		this.worker = worker;
		rule = new HexRule();
	}
	
	public void init(long rule, long iterations) {
		int stateslen = iterations == -1 ? 200000000:(int) Math.min(Math.max(5000000,iterations/(int)Settings.repeatcheck*2), 200000000);
		if(states == null || states.length != stateslen) states = new byte[stateslen];
		this.rule.createRule(rule);
		x = 0;
		y = 0;
		xc = 0;
		yc = 0;		
		dir = 0;
		state = 0;
		saveState = false;
		repeatLength = 0;
		index = 1;
		minHighwayPeriod = 0;
		PERIODFOUND = false;
		chunk = worker.getLevel().chunks.get(0,0);
	}
	
	/**
	 * 
	 * @return true if ant forms a highway
	 */
	public int move() {
		int i = 0;
		for(; i < Settings.itpf; i++) {			
			if(saveState) {
				byte s1 = (byte)(dir<<5 | state); //Only works for rules with <= 32 colors
				if(index < states.length) states[(int) index] = s1;
				index++;
				if(states[repeatLength]!=s1) {
					repeatLength = 0;
					minHighwayPeriod = index;
				} else {
					repeatLength++;
					if(repeatLength == states.length || repeatLength > Settings.repeatcheck*minHighwayPeriod) {
						PERIODFOUND = true;
						saveState = false;
						break;
					}
				}
			}
			
			if(x > Settings.cSIZEm) {
				x = 0;
				xc++;
				chunk = worker.getLevel().getChunk(xc, yc);
			} else if(x < 0) {
				x = Settings.cSIZEm;
				xc--;
				chunk = worker.getLevel().getChunk(xc, yc);
			} 
			if(y > Settings.cSIZEm) {
				y = 0;
				yc++;
				chunk = worker.getLevel().getChunk(xc, yc);
			} else if(y < 0) {
				y = Settings.cSIZEm;
				yc--;
				chunk = worker.getLevel().getChunk(xc, yc);
			}
			
			int index = x|(y<<Settings.cPOW);
			state = chunk.cells[index];
			dir += rule.turn[state];
			if(dir > 5) dir -= 6;
			if(++chunk.cells[index] == rule.size) chunk.cells[index] = 0;
			
			x += directions[dir][0];
			y += directions[dir][1];
			
			//OPTIMIZE (chunk coordinates can only change if x/y = 0/cSIZE)
//			xc += x>>Settings.cPOW;
//			yc += y>>Settings.cPOW;
//			x = x&Settings.cSIZEm;
//			y = y&Settings.cSIZEm;
		}
		return i;
	}
	
	public void saveState() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(rule.rule+".hexstate"));
			oos.writeLong(rule.rule);
			oos.writeLong(worker.getIterations());
			oos.writeInt(dir);
			oos.writeInt(state);
			oos.writeInt(x);
			oos.writeInt(y);
			oos.writeInt(xc);
			oos.writeInt(yc);
			oos.writeBoolean(saveState);
			if(saveState) {
				oos.writeLong(index);
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public long getPeriod() {
		return minHighwayPeriod;
	}

	public boolean findingPeriod() {
		return saveState;
	}

	public boolean periodFound() {
		return PERIODFOUND;
	}

	public int getXC() {
		return xc;
	}

	public int getYC() {
		return yc;
	}

	public void setFindingPeriod(boolean b) {
		saveState = b;
	}

	public void initPeriodFinding() {
		states[0] = (byte)(dir<<5 | state);
	}

	public IRule getRule() {
		return rule;
	}
}