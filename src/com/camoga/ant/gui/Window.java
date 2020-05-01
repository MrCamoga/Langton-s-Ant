package com.camoga.ant.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Window {
	
//	BufferedImage canvasImage = new BufferedImage(Settings.cSIZE*Settings.canvasSize, Settings.cSIZE*Settings.canvasSize, BufferedImage.TYPE_INT_RGB);
//	int[] pixels = ((DataBufferInt) canvasImage.getRaster().getDataBuffer()).getData();
	
	Thread thread;
	boolean running;
	public static JFrame f;
	static Canvas c;
	
	public Window() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		f = new JFrame("Langton's Ant Cellular Automata");
		f.setSize(800, 800);
		gui(f);
		f.setVisible(true);
		f.setResizable(true);
		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void gui(JFrame f) {
		JPanel panel = new JPanel(new GridLayout(1,4));
		
		JMenuBar menu = new JMenuBar();		
			JMenu server = new JMenu("Server");
				ServerActionListener sa = new ServerActionListener();
				JMenuItem connect = new JMenuItem("Connect to Server");
				JMenuItem assign = new JMenuItem("Get Assignments");
				JMenuItem serversettings = new JMenuItem("Settings");

				connect.addActionListener(sa);
				assign.addActionListener(sa);
				serversettings.addActionListener(sa);

				server.add(connect);
				server.add(assign);
				server.add(serversettings);
				
		menu.add(server);
			
		panel.add(menu, BorderLayout.NORTH);
		f.add(panel, BorderLayout.NORTH);
		c = new Canvas();
		f.add(c, BorderLayout.CENTER);
		
	}

//	public void run() {
//		c.createBufferStrategy(3);
//
//		while(running) {
//			render();
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//	}

//	public void render() {
//		Graphics g = c.getBufferStrategy().getDrawGraphics();
//		Level.render(pixels, Settings.canvasSize, canvasImage.getWidth(), canvasImage.getHeight(), Settings.followAnt);
//		
//		g.drawImage(canvasImage, 0, 0, 800, 800, null);
//		g.setColor(Color.WHITE);
//		g.drawString("Iterations: " + Simulation.iterations, 10, 30); 
//		g.drawString("Rule: " + Rule.string(Simulation.rule) + " ("+Simulation.rule+")", 10, 46);
//		
//		if(Ant.saveState) {
//			g.setColor(Color.red);
//			g.drawString("Finding period... " + Ant.minHighwayPeriod, 10, 62);
//		} else if(Ant.CYCLEFOUND) {
//			g.setColor(Color.WHITE);
//			g.drawString("Period: " + Ant.minHighwayPeriod, 10, 62);
//		}
//		
//		g.dispose();
//		getBufferStrategy().show();
//	}
}