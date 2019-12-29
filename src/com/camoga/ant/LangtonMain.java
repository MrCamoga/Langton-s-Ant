package com.camoga.ant;

import java.util.Arrays;

public class LangtonMain {
	static int i = 1;
	public static void main(String[] args) {
		Settings.ignoreSavedRules = true;
		Settings.saverule = true;
		Settings.savepic = true;
		Settings.maxiterations = (long) 1e8;
//		IORules.cleanRulesFile();
//		IORules.saveRulesToTxt();
//		System.exit(0);
//		Settings.detectCycles = false;
		Window window = new Window();
		//31819 --
		//36841
		long r[] = new long[] {13882};
		window.rule = 1;
//		System.out.println("Rules below 100000: " + Arrays.binarySearch(window.savedRules, 100000));

		window.nextrule = new IRule() {
			public long nextRule(long current) {
				long rule;
//				rule = r[i];
//				i++;
				rule = current+1;
//				rule = (long) (Math.random()*100000);
//				rule = (long) (new Random().nextInt(65536)+1);
				return rule;
			}
		};
		window.nextRule();
	}
}

