package com.camoga.ant;

public class Settings {
	
	//GUI
	
	public static int canvasSize = 32; // size of canvas (and output image) in chunks (e.g. scale = 16, cSIZE = 64 => size = 1024x1024)
	public static boolean followAnt = true;
	static boolean smoothFollow = false; //TODO
	public static boolean renderVoid = false; // draws black where no chunk has been generated
	public static int itpf = 1000000; // iterations between frames
	
	//FIND HIGHWAYS
	public static int chunkCheck = 80; // Check if the ant forms a highway when the ant goes further than this chunk from the origin
	public static float repeatpercent = 1.3f; // Number of times the period has to repeat to confirm that it's correct (e.g. You're more certain that 10101010101010101010 has a period of 2 than 1010)
	static boolean autosave = false;
	
	
	//OUTPUT IMAGES
//	static int saveImageW = canvasSize*cSIZE;
//	static int saveImageH = canvasSize*cSIZE;
}