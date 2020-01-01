package com.camoga.ant;

public class Settings {
	
	//GUI
	
	static int canvasSize = 16; // size of canvas (and resulting image) in chunks (e.g. scale = 16, cSIZE = 64 => size = 1024×1024)
	static boolean gui = true;
	static boolean followAnt = true; 
	static boolean smoothFollow = false; //TODO
	static boolean renderVoid = false; // draws black where no chunk has been generated
	static int itpf = 33333334; // iterations between frames
	
	static boolean ignoreSavedRules = true; // If true skips all rules that have already been tested
	static long maxiterations = (long) 1e10; // After this many iterations, program moves to next rule
	
	//LEVEL
	
	/**
	 * 	Map is stored in chunks to increase the speed, changing this value may speed up or slow down the simulation
	 *  Size of chunks = 2^cPOW
	 */
	static final int cPOW = 6;
	static final int cSIZE = 1<<cPOW;
	static final int cSIZEm = cSIZE-1;
	
	//FIND HIGHWAYS
	
	static int chunkCheck = 140; // Check if the ant forms a highway when the ant goes further than this chunk from the origin
	static int repeatcheck = 10; // Number of times the period has to repeat to confirm that it's correct (e.g. You're more certain that 10101010101010101010 has a period of 2 than 1010)
	static boolean detectCycles = true; //Detects if the ant follows a periodic pattern
	
		//Total file size = fileSize*numOfFiles
	static boolean checkCyclesOnDisk = true; //TODO ?
	static long fileSize = (1<<31)-1; // File size in bytes to search for periods. This will create a permanent file of that size on your hard drive
	static long numOfFiles = 3; // Cannot load files bigger than 2GB, this allows you to load bigger files in chunks
	
	//OUTPUT IMAGES

	static final String file = "test2.langton";
	static boolean saverule = true; // saves rule to file
	static boolean savepic = true; // saves pic if ant forms a highway
	static int saveImageW = canvasSize*cSIZE;
	static int saveImageH = canvasSize*cSIZE;
	
	//OTHER
	static boolean deleteOldChunks = false; // only enable when you know old chunks are not going to be visited again
}