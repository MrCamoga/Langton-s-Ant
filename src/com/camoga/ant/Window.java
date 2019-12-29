package com.camoga.ant;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class Window extends Canvas {
	
	static BufferedImage image = new BufferedImage((int)Settings.cSIZE*Settings.canvasSize, (int)Settings.cSIZE*Settings.canvasSize, BufferedImage.TYPE_INT_RGB);
	int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
	
	Thread thread;
	BufferStrategy b;
	
	static Ant ant;
	Level level;
	IRule nextrule = new IRule() {};
	
	String cycles = "";
	
	long[] savedRules; //FIXME new rules are not added to array
	
	public Window() {
		JFrame f = new JFrame("Langton's Ant - by MrCamoga");
		f.setSize(800, 800);
		gui(f);
		f.setVisible(true);
		f.setResizable(true);
		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_SPACE) {
					space = true;
					
				} else if(e.getKeyCode() == KeyEvent.VK_S && !ant.CYCLEFOUND) ant.saveState = true;
			}
		});
		if(Settings.ignoreSavedRules) savedRules = IORules.searchSavedRules();
	}

	public void gui(JFrame f) {
		JPanel panel = new JPanel(new GridLayout(1,4));
		JSlider speed = new JSlider(1, 40000000, itpf);
			speed.setOrientation(JSlider.HORIZONTAL);
			speed.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					itpf = ((JSlider)e.getSource()).getValue();
				}
			});
		JButton pic = new JButton("Take Picture");
			pic.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveImage(rule+"");
				}
			});

		JButton checkCycle = new JButton("Check Cycle");
		
		panel.add(speed);
		panel.add(pic);
		panel.add(checkCycle);
		f.add(panel, BorderLayout.NORTH);
		f.add(this, BorderLayout.CENTER);
	}
	
	public void saveRule() {
		try {
			if(Settings.savepic) {
				if(ant.CYCLEFOUND || ant.saveState) {
					File dir = new File(ant.minCycleLength+"");
					boolean newdir = !dir.exists() ? dir.mkdir():false;
					cycles += rule + "\t" + ant.minCycleLength + "\t" + (ant.highwaystart) + (newdir ? " N":"")+"\n";
					if(ant.CYCLEFOUND) {
						saveImage(ant.minCycleLength + "/"+rule);
					} else {
						saveImage("0/"+rule);
						System.gc();
					}					
				}	
			}
			if(!Settings.saverule) return;
			FileOutputStream fos = new FileOutputStream("test2.langton", true);
			fos.write(ByteBuffer.allocate(12).putLong(rule).putInt((int)ant.minCycleLength).array());
			fos.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	boolean space = false;
	boolean render = true;
	long rule = -1;
	
	static long iterations = 0;
	static int itpf = 100;

	public void run() {
		createBufferStrategy(3);
		b = getBufferStrategy();
		double timer = System.currentTimeMillis();
		while(true) {
			int i = 0;
			for(; i < itpf; i++) {
				if(ant.move()) break;
			}
			
			if(System.currentTimeMillis() - timer > 1000) {
				timer = System.currentTimeMillis();
//				System.out.println(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
			}
			iterations += i;
			if(iterations > Settings.maxiterations || ant.CYCLEFOUND) {
				saveRule();
				System.out.println(cycles);
				space = true;
			}
			if(render)	render(b, b.getDrawGraphics());

			if(space) {
				rule = nextrule.nextRule(rule);
				nextRule();
			}
		}
	}
	
	boolean random = false;
	
	public void nextRule() {
		//I exclude rules I already know they form a highway and its cycle length
		while((savedRules != null && Arrays.binarySearch(savedRules, rule) >= 0) || ignoreRules()) {
			rule = nextrule.nextRule(rule);
		}
		System.out.println(rule);
		if(Level.chunks != null) Level.chunks.clear();
		level = new Level();
		System.gc();
		new Rule(rule);
		ant = new Ant(0,0);
		iterations = 0;
		space = false;
		
		if(thread == null) {
			thread = new Thread(() -> run());
			thread.start();
		}
	}
	
	protected void saveImage(String file) {
		render = false;
		render(b, image.createGraphics());
		try {
			ImageIO.write(image, "PNG", new File(file+".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		render = true;
	}
	
	private boolean ignoreRules() {		
		if(((rule+1) & (rule)) == 0) return true;			// 2^n - 1
		if((rule & (rule-1)) == 0) return true; // 2^n
		if(rule % 512 == 469 || (rule % 512 == 42 && rule > 512)) return true; // ?
		if(rule%2==0) { // RL...LRL...L
			long kexp = (rule&-rule);
			int k = 63 - Long.numberOfLeadingZeros(kexp);
			if(k >= 2) {
				long x = (rule/kexp-1);
				if(x%(kexp<<1) == 0) {
					double n = (x/(kexp<<1)+1)/2.0;
					if(n%1==0 && n>= 1) {
						return true;
					}
				}
			}
		} else { // LR...RLR...R
			long kexp = (rule+1)&-(rule+1);
			int k = 63 - Long.numberOfLeadingZeros(kexp);
			if(k >= 2) {
				long x = (rule+1)/kexp+1;
				if(x%(kexp<<1) == 0) {
					double n = (x/(kexp<<1)-1)/2.0;
					if(n%1==0 && n >= 1) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void render(BufferStrategy b, Graphics g) {
		for(int i = 0; i < pixels.length; i++) {
			pixels[i] = Rule.colors.get(0).color;
		}
		Level.render(pixels, Settings.canvasSize);
		
		if(render)
		g.drawImage(image, 0, 0, 800, 800, null);
		g.setColor(Color.WHITE);
		g.drawString("Iterations: " + iterations, 10, 30); 
		g.drawString("Rule: " + Rule.string() + " ("+rule+")", 10, 46);
		if(ant.saveState) {
			g.setColor(Color.red);
			g.drawString("Finding cycle... " + ant.minCycleLength, 10, 62);
		} else if(ant.CYCLEFOUND) {
			g.setColor(Color.WHITE);
			g.drawString("Cycle found: " + ant.minCycleLength, 10, 62);
		}
		
		g.dispose();
		b.show();
	}
}
