package com.camoga.ant;

public class Settings {
	// scale*200
	static int scale = 4;
	
	static boolean ignoreSavedRules = true; // If true skips all rules that have already been tested
	
	static boolean detectCycles = true; // If true detects if ant follows a periodic pattern
	static boolean saverule = true; // saves rule to file
	static boolean savepic = true; // saves pic if ant forms a highway
	
	static boolean gui = true;
}