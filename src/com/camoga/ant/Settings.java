package com.camoga.ant;

public class Settings {
	
	//GUI
	
	final static int canvasSize = 8; // size of canvas (and output image) in chunks (e.g. scale = 16, cSIZE = 64 => size = 1024x1024)
	static boolean gui = true;
	static boolean followAnt = true;
	static boolean smoothFollow = false; //TODO
	static boolean renderVoid = false; // draws black where no chunk has been generated
	static int itpf = 33333334; // iterations between frames
	static boolean printlog = true;
	
	//LEVEL
	
	/**
	 * 	Map is stored in chunks
	 *  Size of chunks = 2^cPOW
	 */
	static final int cPOW = 7;
	static final int cSIZE = 1<<cPOW;
	static final int cSIZEm = cSIZE-1;
	
	//FIND HIGHWAYS
	
	static boolean ignoreSavedRules = true; // If true skips all rules that have already been tested
	static final String file = "ruleperiods.langton";
	static final String statefile = "langton.buf";
	static int chunkCheck = 90; // Check if the ant forms a highway when the ant goes further than this chunk from the origin
	static float repeatcheck = 40; // Number of times the period has to repeat to confirm that it's correct (e.g. You're more certain that 10101010101010101010 has a period of 2 than 1010)
	static boolean detectHighways = true; //Detects if the ant follows a periodic pattern
	static long maxiterations = (long) 1e8; // After this many iterations, program moves to next rule
	static boolean saverule = true; // saves rule to file
	
		//Total file size = fileSize*numOfFiles
	static long fileChunkSize = (1<<29)-1; // File size in bytes to search for periods. This will create a permanent file of that size on your hard drive
	static long maxNumOfChunks = 2000; // MappedByteBuffer cannot load files bigger than 2GB, this allows you to load bigger files in chunks
	
	//OUTPUT IMAGES
	static boolean toot = false;
	static boolean savepic = false; // saves pic if ant forms a highway
	static int saveImageW = canvasSize*cSIZE;
	static int saveImageH = canvasSize*cSIZE;
	
	//OTHER
	static boolean deleteOldChunks = false; // only enable when you know old chunks are not going to be visited again
}