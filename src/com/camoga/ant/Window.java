package com.camoga.ant;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.camoga.ant.Level.Chunk;

@SuppressWarnings("serial")
public class Window extends Canvas {
	
	static BufferedImage canvasImage = new BufferedImage(Settings.cSIZE*Settings.canvasSize, Settings.cSIZE*Settings.canvasSize, BufferedImage.TYPE_INT_RGB);
	int[] pixels = ((DataBufferInt) canvasImage.getRaster().getDataBuffer()).getData();
	
	Thread thread;
	
	
	public Window() {
		JFrame f = new JFrame("Langton's Ant - by MrCamoga");
		f.setSize(800, 800);
		gui(f);
		f.setVisible(true);
		f.setResizable(true);
		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		new Thread(() -> run(), "Render Thread").start();
	}

	public void gui(JFrame f) {
		JPanel panel = new JPanel(new GridLayout(1,4));
		JSlider speed = new JSlider(1, Math.max(Settings.itpf, 40000000), Settings.itpf);
			speed.setOrientation(JSlider.HORIZONTAL);
			speed.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					Settings.itpf = ((JSlider)e.getSource()).getValue();
				}
			});
		JButton pic = new JButton("Take Picture");
			pic.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						ImageIO.write(canvasImage, "png", new File(Rule.string()+".png"));						
					} catch(IOException e2) {
						e2.printStackTrace();
					}
				}
			});

//		JButton checkCycle = new JButton("Check Cycle");
		
		panel.add(speed);
		panel.add(pic);
//		panel.add(checkCycle);
		f.add(panel, BorderLayout.NORTH);
		f.add(this, BorderLayout.CENTER);
	}

	public void run() {
		createBufferStrategy(3);

		while(true) {
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
		
		if(Simulation.ant.saveState) {
			g.setColor(Color.red);
			g.drawString("Finding highway... " + Simulation.ant.minHighwayPeriod, 10, 62);
		} else if(Simulation.ant.CYCLEFOUND) {
			g.setColor(Color.WHITE);
			g.drawString("Period: " + Simulation.ant.minHighwayPeriod, 10, 62);
		}
		
		g.dispose();
		getBufferStrategy().show();
	}
}