package com.camoga.ant;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class Window extends Canvas {
	
	static BufferedImage canvasImage = new BufferedImage(Settings.cSIZE*Settings.canvasSize, Settings.cSIZE*Settings.canvasSize, BufferedImage.TYPE_INT_RGB);
	int[] pixels = ((DataBufferInt) canvasImage.getRaster().getDataBuffer()).getData();
	
	Thread thread;
	boolean running;
	
	
	public Window() {
		JFrame f = new JFrame("Langton's Ant - by MrCamoga");
		f.setSize(800, 800);
		gui(f);
		f.setVisible(true);
		f.setResizable(true);
		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				stop();
			}
		});
		start();
	}
	
	public void start() {
		running = true;
		thread = new Thread(() -> run(), "Render Thread");
		thread.start();
	}
	
	public void stop() {
		running = false;
	}

	public void gui(JFrame f) {
		JPanel panel = new JPanel(new GridLayout(1,4));
//		JSlider speed = new JSlider(1, Math.max(Settings.itpf, 40000000), Settings.itpf);
//			speed.setOrientation(JSlider.HORIZONTAL);
//			speed.addChangeListener(new ChangeListener() {
//				public void stateChanged(ChangeEvent e) {
//					Settings.itpf = ((JSlider)e.getSource()).getValue();
//				}
//			});
		JButton pic = new JButton("Take Picture");
			pic.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Simulation.saveImage(new File(Rule.string()+".png"));
				}
			});
		JButton pause = new JButton("Pause");
		pause.addActionListener(e -> {
			String cmd = e.getActionCommand();
			if(cmd.equals("Pause")) {
				pause.setText("Resume");
				Simulation.stop();
			} else {
				pause.setText("Pause");
				Simulation.start();
			}
		});
		JButton savestate = new JButton("Save State");
		savestate.addActionListener(e -> {
			Simulation.stop();
			pause.setText("Resume");
			while(!Simulation.finished)
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			LangtonMain.saveState();
		});

//		JButton checkCycle = new JButton("Check Cycle");
		
//		panel.add(speed);
		panel.add(pic);
		panel.add(pause);
		panel.add(savestate);
//		panel.add(checkCycle);
		f.add(panel, BorderLayout.NORTH);
		f.add(this, BorderLayout.CENTER);
	}

	public void run() {
		createBufferStrategy(3);

		while(running) {
			render();
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void render() {
		Graphics g = getBufferStrategy().getDrawGraphics();
		Level.render(pixels, Settings.canvasSize, canvasImage.getWidth(), canvasImage.getHeight(), true);
		
		g.drawImage(canvasImage, 0, 0, 800, 800, null);
		g.setColor(Color.WHITE);
		g.drawString("Iterations: " + Simulation.iterations, 10, 30); 
		g.drawString("Rule: " + Rule.string(Simulation.rule) + " ("+Simulation.rule+")", 10, 46);
		
		if(Ant.saveState) {
			g.setColor(Color.red);
			g.drawString("Finding period... " + Ant.minHighwayPeriod, 10, 62);
		} else if(Ant.CYCLEFOUND) {
			g.setColor(Color.WHITE);
			g.drawString("Period: " + Ant.minHighwayPeriod, 10, 62);
		}
		
		g.dispose();
		getBufferStrategy().show();
	}
}