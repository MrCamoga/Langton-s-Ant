package com.camoga.ant;

public class LangtonMain {
//	static int i = 1;
	public static void main(String[] args) {
		Settings.ignoreSavedRules = true;
		Settings.saverule = true;
		Settings.savepic = true;
		Settings.maxiterations = (long) 1e8;
		Settings.canvasSize = 16;
		Settings.chunkCheck = 120;
		IORules.cleanRulesFile();
//		IORules.exportToNewFormat();
//		IORules.getInfo();
//		IORules.saveRulesToTxt();
		System.exit(0);
		Window window = new Window();
		//36841,  851019 ,1260619, 786123 period: 5307264488, 7208011 owo
//		long r[] = IORules.searchRules(0);
		window.rule = 100000;//
//		System.out.println("Rules below 100000: " + Arrays.binarySearch(window.savedRules, 100000));
		window.nextrule = new IRule() {
			public long nextRule(long current) {
				long rule;
//				rule = r[i];
//				i++;
				rule = current+1;
//				rule = (long) (Math.random()*1000000);
				return rule;
			}
		};
		window.nextRule();
	}
	
	public static void testHighways() {
		
	}
}