package com.camoga.ant.ants;

public class AntFactory {
	private AntFactory() {}

	public static AbstractAnt createAnt(int type) {
		switch(type) {
			case 0: return new Ant();
			case 1: return new AntHex();
			case 2: return new Ant3D();
			case 3: return new Ant4D();
			default: throw new IllegalArgumentException();
		}
	}
}
