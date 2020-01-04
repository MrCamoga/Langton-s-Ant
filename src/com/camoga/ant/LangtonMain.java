package com.camoga.ant;

public class LangtonMain {
	
	public static void main(String[] args) {
		Window window = new Window();
		
		window.rule = 12892;
		window.nextrule = r -> r+1;
		
		window.nextRule();
	}
}