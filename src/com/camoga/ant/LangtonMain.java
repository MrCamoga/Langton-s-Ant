package com.camoga.ant;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.imageio.ImageIO;

public class LangtonMain {
	
	static int i = 70;
	
	public static void main(String[] args) throws Exception {
		Settings.maxiterations = (long) 3e20;
		Settings.toot = true;
		Settings.savepic = true;
		Settings.ignoreSavedRules = false;
		Settings.deleteOldChunks = true;
		Settings.chunkCheck = 200;

//		IORules.cleanRulesFile();
		IORules.getInfo();
		System.exit(0);
		
//		LLRRRLRLRR                        231929
//		long[] rules = new long[] {28540215,1572151,12682164,15416631,14318903,10960183};
//		for(long rule : rules) System.out.println(rule + ", " + Rule.string(rule));
		BufferedReader br = new BufferedReader(new FileReader("settings.txt"));
		long rule = Long.parseLong(br.readLine().split(" ")[0]);
		long step = Long.parseLong(br.readLine().split(" ")[0]);
		Settings.followAnt = Boolean.parseBoolean(br.readLine().split(" ")[0]);
		Settings.itpf = Integer.parseInt(br.readLine().split(" ")[0]);
		br.close();

		Simulation sim = new Simulation(13730100, r -> r+8192);
		createSystemTray();
		if(Settings.gui) new Window();
	}

	//TODO
	private static void createSystemTray() throws AWTException {
		SystemTray tray = SystemTray.getSystemTray();
		PopupMenu popup = new PopupMenu();
		popup.add(new MenuItem("test"));
		TrayIcon icon = new TrayIcon(Toolkit.getDefaultToolkit().getImage("C:\\Users\\usuario\\workspace\\CELLULAR AUTOMATA\\Langton-s-Ant\\res\\icon.png"), "Langton's Ant", popup);
		icon.setImageAutoSize(true);
		tray.add(icon);
		icon.addActionListener(e -> System.out.println(e.getActionCommand()));
		
	}
}