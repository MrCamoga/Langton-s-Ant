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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class LangtonMain {
	
	public static SystemTray tray;
	
	public static void main(String[] args) throws Exception {
		main();
//		loadRule();
		createSystemTray();
	}
	
	public static void loadRule() {
		Settings.maxiterations = -1;
		Settings.savepic = true;
		Settings.ignoreSavedRules = false;
		Settings.deleteOldChunks = true;
		Settings.chunkCheck = 200;
		Settings.gui = true;
		new Simulation("RRLRLLRLLLRRRRRRR.dat", r -> r+1);
		if(Settings.gui) new Window();
	}
	
	public static void main() throws Exception {
		Settings.maxiterations = -1;
//		Settings.toot = true;
		Settings.savepic = true;
		Settings.ignoreSavedRules = false;
		Settings.deleteOldChunks = true;
		Settings.chunkCheck = 200;
		Settings.gui = true;
//		IORules.cleanRulesFile();
//		IORules.getInfo();
//		IORules.saveRulesToTxt();
//		System.exit(0);
		
//		LLRRRLRLRR                        231929
//		long[] rules = new long[] {28540215 no,12682164,14318903,10960183};
		
		BufferedReader br = new BufferedReader(new FileReader("settings.txt"));
		long rule = Long.parseLong(br.readLine().split(" ")[0]);
		long step = Long.parseLong(br.readLine().split(" ")[0]);
		Settings.followAnt = Boolean.parseBoolean(br.readLine().split(" ")[0]);
		Settings.itpf = Integer.parseInt(br.readLine().split(" ")[0]);
		br.close();

		Simulation sim = new Simulation(130123, r -> r+8192);
		if(Settings.gui) new Window();
	}
	
	public static void checkRules() {
		Settings.maxiterations = (long) 2e8;
		Settings.chunkCheck = 70;

		new Simulation(1, r -> r+1);
		if(Settings.gui) new Window();
	}

	//TODO
	private static void createSystemTray() throws AWTException {
		tray = SystemTray.getSystemTray();
		PopupMenu popup = new PopupMenu();

		
		MenuItem pause = new MenuItem("Pause");
		MenuItem save = new MenuItem("Save State");
		MenuItem open = new MenuItem("Open State");
		ActionListener event = e -> {
			switch(e.getActionCommand()) {
			case "Pause":
				pause.setLabel("Resume");
				Simulation.stop();
				break;
			case "Resume":
				pause.setLabel("Pause");
				Simulation.start();
				break;
			case "Save State":
				Simulation.stop();
				pause.setLabel("Resume");
				while(!Simulation.finished)
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				saveState();
				break;
			case "Open State":
				if(Simulation.running) {
					Simulation.stop();
					pause.setLabel("Resume");
					while(!Simulation.finished)
						try {
							Thread.sleep(100);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
				}
				if(JOptionPane.showConfirmDialog(null, "Ant is running, do you want to save the ant before loading another one?", "Warning", JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
					saveState();
				}
				JFileChooser filechooser = new JFileChooser(".");
				filechooser.showOpenDialog(null);
				File file = filechooser.getSelectedFile();
				System.out.println(file);
				if(file == null) return;
				new Simulation(file.toString(), r -> r+1);
				pause.setLabel("Pause");
			}
		};
		
		pause.addActionListener(event);
		save.addActionListener(event);
		open.addActionListener(event);
		
		popup.add(pause);
		popup.add(open);
		popup.add(save);
		
		TrayIcon icon = new TrayIcon(Toolkit.getDefaultToolkit().getImage("C:\\Users\\usuario\\workspace\\CELLULAR AUTOMATA\\Langton-s-Ant\\res\\icon.png"), "Langton's Ant", popup);
		icon.setImageAutoSize(true);
		tray.add(icon);
		icon.addActionListener(e -> System.out.println(e.getActionCommand()));
		
	}
	
	public static void saveState() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(Rule.string() + ".dat"));
//			oos.write(Settings.cPOW); 
			//Ant
			oos.writeLong(Simulation.iterations);
			oos.writeLong(Simulation.rule);
			oos.writeInt(Ant.dir);
			oos.writeInt(Ant.x);
			oos.writeInt(Ant.y);
			oos.writeInt(Ant.xc);
			oos.writeInt(Ant.yc);
			oos.writeBoolean(Ant.saveState);
			if(Ant.saveState) {
				oos.writeLong(Ant.index);
				oos.writeInt(Ant.currentCycleLength);
				oos.writeLong(Ant.minHighwayPeriod);
				oos.write(Ant.states);
			}
			//Level
			oos.writeObject(Level.chunks);
			oos.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}