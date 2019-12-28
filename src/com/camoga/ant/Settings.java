package com.camoga.ant;

public class Settings {
	// scale*200
	static int scale = 4;
	static boolean gui = true;
	
	static boolean ignoreSavedRules = true; // If true skips all rules that have already been tested

	static long maxiterations = (long) 2e8; // After this many iterations, program moves to next rule
	
	/**
	 * 	Map is stored in chunks to increase the speed, changing this value may speed up or slow down the simulation
	 *  Chunks are powers of two because bitwise op are faster
	 *  If you don't put a power of two the program will break
	 */
	static final int cSIZE = 64; 
	
	static int chunkCheck = 60; // Check if the ant forms a highway if the ant goes further than this chunk from the origin
	static boolean detectCycles = true; //Detects if the ant follows a periodic pattern
	static boolean checkCyclesOnDisk = false; // Slower but does not use RAM (rules with huge periods can use GBs of RAM)
	static boolean saverule = true; // saves rule to file
	static boolean savepic = true; // saves pic if ant forms a highway
	
}