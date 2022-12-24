package com.camoga.ant;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Version {
	
	protected int[] version = new int[3];
	
	public Version(int major, int minor, int patch) {
		version[0] = major;
		version[1] = minor;
		version[2] = patch;
	}
	
	public int getMajor() { return version[0]; }
	public int getMinor() { return version[1]; }
	public int getPatch() { return version[2]; }
	
	public String toString() {
		return Arrays.stream(version).mapToObj(String::valueOf).collect(Collectors.joining("."));
	}
	
	/**
	 * 
	 * @param v1 
	 * @param v2
	 * @return -1 if v1 < v2, 0 if v1 == v2 and 1 if v1 > v2
	 */
	// 1.0.12
	// 0.0.13
	//
	public static int compare(Version v1, Version v2) {
		for(int i = 0, sub = 0; i < 3; i++) {
			sub = v1.version[i]-v2.version[i];
			if(sub<0) return -1;
			if(sub>0) return 1;
		}
		return 0;
	}
	
	public int compareTo(Version other) {
		return compare(this, other);
	}
}