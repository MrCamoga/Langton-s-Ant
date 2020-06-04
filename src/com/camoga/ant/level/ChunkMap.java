package com.camoga.ant.level;

import java.util.HashMap;

import com.camoga.ant.level.Level.Chunk;

public class ChunkMap extends HashMap<MultiKey, Chunk> {
	
	public Chunk put(int x, int y, Chunk chunk) {
		return put(new MultiKey(x, y), chunk);
	}
	
	public Chunk get(int x, int y) {
		return get(new MultiKey(x, y));
	}
}