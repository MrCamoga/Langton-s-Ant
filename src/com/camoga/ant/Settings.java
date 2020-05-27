package com.camoga.ant;

public class Settings {
	
	//GUI
	
	public static int canvasSize = 8; // size of canvas (and output image) in chunks (e.g. scale = 16, cSIZE = 64 => size = 1024x1024)
	static boolean followAnt = true;
	static boolean smoothFollow = false; //TODO
	static boolean renderVoid = false; // draws black where no chunk has been generated
	static int itpf = 33333334; // iterations between frames
	
	//LEVEL
	
	/**
	 * 	Map is stored in chunks
	 *  Size of chunks = 2^cPOW
	 */
	static int cPOW = 7;
	public static int cSIZE = 1<<cPOW;
	static int cSIZEm = cSIZE-1;
	
	public static void setChunkSize(int pow) {
		if(pow < 1 || pow > 10) throw new RuntimeException("Invalid chunk size");
		cPOW = pow;
		cSIZE = 1<<pow;
		cSIZEm = cSIZE-1;
	}
	
	//FIND HIGHWAYS
	static int chunkCheck = 90; // Check if the ant forms a highway when the ant goes further than this chunk from the origin
	static float repeatcheck = 40; // Number of times the period has to repeat to confirm that it's correct (e.g. You're more certain that 10101010101010101010 has a period of 2 than 1010)
	static boolean autosave = false;
	
	static int highwaySizew = 400000;
	static int highwaySizeh = 800;
	
	
	//OUTPUT IMAGES
	static boolean toot = false;
	static boolean savepic = false; // saves pic if ant forms a highway
	static int saveImageW = canvasSize*cSIZE;
	static int saveImageH = canvasSize*cSIZE;
//	static int saveImageW = 100000;
//	static int saveImageH = 1200;
	
	//OTHER
	static boolean deleteOldChunks = false; // only enable when you know old chunks are not going to be visited again
}