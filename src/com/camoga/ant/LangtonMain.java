package com.camoga.ant;

public class LangtonMain {
	
////		LLRRRLRLRR 

		
//	private static void createSystemTray() throws AWTException {
//		tray = SystemTray.getSystemTray();
//		PopupMenu popup = new PopupMenu();
		
//		MenuItem pause = new MenuItem("Pause");
//		MenuItem save = new MenuItem("Save State");
//		MenuItem open = new MenuItem("Open State");
//		MenuItem showwindow = new MenuItem("Show Window");
//		MenuItem exit = new MenuItem("Exit");
//		ActionListener event = e -> {
//			switch(e.getActionCommand()) {
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
//			case "Show Window":
//				if(Window.f == null) new Window();
//				else Window.f.setVisible(true);
//				break;
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
//			}
//		};
		
//		pause.addActionListener(event);
//		save.addActionListener(event);
//		open.addActionListener(event);
//		showwindow.addActionListener(event);
		
//		popup.add(pause);
//		popup.add(open);
//		popup.add(save);
//		popup.add(showwindow);
//		
//		TrayIcon icon = new TrayIcon(Toolkit.getDefaultToolkit().getImage("C:\\Users\\usuario\\workspace\\CELLULAR AUTOMATA\\Langton-s-Ant\\res\\icon.png"), "Langton's Ant", popup);
//		icon.setImageAutoSize(true);
//		tray.add(icon);
//		icon.addActionListener(e -> System.out.println(e.getActionCommand()));
		
}


//231929
//530921
//1259753
//2147252 
//10960183
//11010356 
//12532811
//12682164
//14318903
//15401672
//15729352
//15860424
//15940683
//28540215
//21757003
//21889332 
//25609140
//36045748 
//41892939
//44514379 
//49495115
//56360651 
//58244811