package com.camoga.ant;

public class Settings {
	
	//GUI
	
	public static int canvasSize = 8; // size of canvas (and output image) in chunks (e.g. scale = 16, cSIZE = 64 => size = 1024x1024)
	public static boolean followAnt = false;
	static boolean smoothFollow = false; //TODO
	public static boolean renderVoid = false; // draws black where no chunk has been generated
	public static int itpf = 33333334; // iterations between frames
	
	//LEVEL
	
	/**
	 * 	Map is stored in chunks
	 *  Size of chunks = 2^cPOW
	 */
	public static int cPOW = 7;
	public static int cSIZE = 1<<cPOW;
	public static int cSIZEm = cSIZE-1;
	
	public static void setChunkSize(int pow) {
		if(pow < 1 || pow > 10) throw new RuntimeException("Invalid chunk size");
		cPOW = pow;
		cSIZE = 1<<pow;
		cSIZEm = cSIZE-1;
	}
	
	//FIND HIGHWAYS
	public static int chunkCheck = 70; // Check if the ant forms a highway when the ant goes further than this chunk from the origin
	public static float repeatcheck = 1000; // Number of times the period has to repeat to confirm that it's correct (e.g. You're more certain that 10101010101010101010 has a period of 2 than 1010)
	static boolean autosave = false;
	
	
	//OUTPUT IMAGES
	static int saveImageW = canvasSize*cSIZE;
	static int saveImageH = canvasSize*cSIZE;
}