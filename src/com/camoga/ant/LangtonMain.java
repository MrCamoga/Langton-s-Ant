package com.camoga.ant;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import com.camoga.ant.gui.Window;
import com.camoga.ant.test.net.Client;

public class LangtonMain {
	
	public static SystemTray tray;
	
////		LLRRRLRLRR                        231929
////		long[] rules = new long[] {28540215 no,12682164,14318903,10960183};

	
	static int i = 0;
	static int k = 0;
	public static void checkRules() {
		Settings.maxiterations = (long) 1.2e8;

		Settings.savepic = true;
		Settings.chunkCheck = 70;

//		Simulation.init(1, r-> r+1); //1259753

		// 2147252 11010356 21889332 36045748 56360651 
//		Duplicated: 15401672, new cycle: 0; old cycle: 1
//		Duplicated: 15729352, new cycle: 0; old cycle: 1
//		Duplicated: 15860424, new cycle: 0; old cycle: 1
		//	OWO 530921
		
	
		// 21889332 
		// 12532811 15940683 21757003 25609140 41892939 44514379 49495115
	}

	private static void createSystemTray() throws AWTException {
		tray = SystemTray.getSystemTray();
		PopupMenu popup = new PopupMenu();

		
//		MenuItem pause = new MenuItem("Pause");
//		MenuItem save = new MenuItem("Save State");
//		MenuItem open = new MenuItem("Open State");
		MenuItem showwindow = new MenuItem("Show Window");
		MenuItem exit = new MenuItem("Exit");
		ActionListener event = e -> {
			switch(e.getActionCommand()) {
//			case "Pause":
//				pause.setLabel("Resume");
//				Simulation.stop();
//				break;
//			case "Resume":
//				if(Simulation.start()) pause.setLabel("Pause");
//				break;
//			case "Save State":
//				Simulation.stop();
//				pause.setLabel("Resume");
//				while(!Simulation.finished)
//					try {
//						Thread.sleep(100);
//					} catch (InterruptedException e1) {
//						e1.printStackTrace();
//					}
//				saveState();
//				break;
//			case "Open State":
//				if(Simulation.running) {
//					Simulation.stop();
//					pause.setLabel("Resume");
//					while(!Simulation.finished)
//						try {
//							Thread.sleep(100);
//						} catch (InterruptedException e1) {
//							e1.printStackTrace();
//						}
//				}
//				if(JOptionPane.showConfirmDialog(null, "Ant is running, do you want to save the ant before loading another one?", "Warning", JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
//					saveState();
//				}
//				JFileChooser filechooser = new JFileChooser(".");
//				filechooser.setFileFilter(new FileNameExtensionFilter(".dat file type", "dat"));
//				filechooser.showOpenDialog(null);
//				File file = filechooser.getSelectedFile();
//				System.out.println(file);
//				if(file == null) return;
//				new Simulation(file.toString(), r -> r+1);
//				pause.setLabel("Pause");
//				break;
			case "Show Window":
				if(Window.f == null) new Window();
				else Window.f.setVisible(true);
				break;
//			case "Exit":
//				Simulation.stop();
//				pause.setLabel("Resume");
//				while(!Simulation.finished)
//					try {
//						Thread.sleep(100);
//					} catch (InterruptedException e1) {
//						e1.printStackTrace();
//					}
//				if(JOptionPane.showConfirmDialog(null, "Do you want to save the ant before exiting?", "Warning", JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
//					saveState();
//				}
//				System.exit(0);
			}
		};
		
//		pause.addActionListener(event);
//		save.addActionListener(event);
//		open.addActionListener(event);
		showwindow.addActionListener(event);
		
//		popup.add(pause);
//		popup.add(open);
//		popup.add(save);
		popup.add(showwindow);
		
		TrayIcon icon = new TrayIcon(Toolkit.getDefaultToolkit().getImage("C:\\Users\\usuario\\workspace\\CELLULAR AUTOMATA\\Langton-s-Ant\\res\\icon.png"), "Langton's Ant", popup);
		icon.setImageAutoSize(true);
		tray.add(icon);
		icon.addActionListener(e -> System.out.println(e.getActionCommand()));
		
	}
	
	public static void saveState() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(Simulation.rule + ".dat"));
//			oos.write(Settings.cPOW); 
			//Ant
			oos.writeLong(Simulation.iterations);
			oos.writeLong(Simulation.rule);
			oos.writeInt(Ant.dir);
			oos.writeInt(Ant.state);
			oos.writeInt(Ant.x);
			oos.writeInt(Ant.y);
			oos.writeInt(Ant.xc);
			oos.writeInt(Ant.yc);
			oos.writeBoolean(Ant.saveState);
			if(Ant.saveState) {
				oos.writeLong(Ant.index);
				oos.writeInt(Ant.repeatLength);
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