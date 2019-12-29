package com.camoga.ant;

public class Settings {
	
	
	static int canvasSize = 16; // size of canvas (and resulting image) in chunks (e.g. scale = 16, cSIZE = 64 => size = 1024×1024)
	static boolean gui = true;
	
	static boolean ignoreSavedRules = true; // If true skips all rules that have already been tested

	static long maxiterations = (long) 1e10; // After this many iterations, program moves to next rule
	
	/**
	 * 	Map is stored in chunks to increase the speed, changing this value may speed up or slow down the simulation
	 *  Chunks are powers of two because bitwise op are faster
	 *  If you don't put a power of two the program will break
	 */
	static final int cSIZE = 64; 
	
	static int chunkCheck = 140; // Check if the ant forms a highway if the ant goes further than this chunk from the origin
	static boolean detectCycles = true; //Detects if the ant follows a periodic pattern
	static boolean checkCyclesOnDisk = true;
	static long fileSize = (1<<31)-1; // File size in bytes to search for periods. This will create a permanent file of that size on your hard drive
	
	static boolean saverule = true; // saves rule to file
	static boolean savepic = true; // saves pic if ant forms a highway
	
}