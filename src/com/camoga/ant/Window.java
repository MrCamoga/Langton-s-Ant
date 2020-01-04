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

import com.camoga.ant.Level.Chunk;

@SuppressWarnings("serial")
public class Window extends Canvas {
	
	static BufferedImage canvasImage = new BufferedImage(Settings.cSIZE*Settings.canvasSize, Settings.cSIZE*Settings.canvasSize, BufferedImage.TYPE_INT_RGB);
	int[] pixels = ((DataBufferInt) canvasImage.getRaster().getDataBuffer()).getData();
	
	Thread thread;
	
	static Ant ant;
	IRule nextrule = c -> c+1;
	
	String log = "";
	
	long[] savedRules; //FIXME new rules are not added to array
	
	public Window() {
		JFrame f = new JFrame("Langton's Ant - by MrCamoga");
		f.setSize(800, 800);
		gui(f);
		f.setVisible(true);
		f.setResizable(true);
		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		if(Settings.ignoreSavedRules) savedRules = IORules.searchSavedRules(false);
	}

	public void gui(JFrame f) {
		JPanel panel = new JPanel(new GridLayout(1,4));
		JSlider speed = new JSlider(1, 40000000, Settings.itpf);
			speed.setOrientation(JSlider.HORIZONTAL);
			speed.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					Settings.itpf = ((JSlider)e.getSource()).getValue();
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
				if(ant.CYCLEFOUND) {
					if(Settings.savepic) {
						File dir = new File(ant.minHighwayPeriod+"");
						boolean newdir = !dir.exists() ? dir.mkdir():false;
						log += rule + "\t" + ant.minHighwayPeriod + "\t" + (newdir ? " N":"")+"\n";
						saveImage(ant.minHighwayPeriod + "/"+rule);
					} else log += rule + "\t" + ant.minHighwayPeriod + "\n";
				} else if(ant.saveState) {
					log += rule + "\t" + "?" +"\n";
					if(Settings.savepic) saveImage(0 + "/" + rule);
				}
			if(!Settings.saverule) return;
			FileOutputStream fos = new FileOutputStream(Settings.file, true);
			fos.write(ByteBuffer.allocate(16).putLong(rule).putLong((int) (ant.CYCLEFOUND ? ant.minHighwayPeriod:(ant.saveState ? 1:0))).array());
			fos.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	long rule = 1;
	
	static long iterations = 0;

	public void run() {
		createBufferStrategy(3);

		while(true) {
			int i = 0;
			for(; i < Settings.itpf; i++) {
				if(ant.move()) break;
			}
			
			if(Settings.deleteOldChunks) {
				Level.chunks.removeIf((Chunk c) -> iterations - c.lastVisit >= 100000000);
			}

			iterations += i;
			render();
			if(iterations > Settings.maxiterations || ant.CYCLEFOUND) {
				saveRule();
				System.out.println(log);
				rule = nextrule.nextRule(rule);
				nextRule();
			}
		}
	}
	
	public void nextRule() {
		//I exclude rules I already know they form a highway and its cycle length
		while((savedRules != null && Arrays.binarySearch(savedRules, rule) >= 0) || ignoreRules()) {
			rule = nextrule.nextRule(rule);
		}
		System.out.println(rule);
		Level.init();
		Rule.createRule(rule);
		ant = new Ant(0,0);
		iterations = 0;
		
		if(thread == null) {
			thread = new Thread(() -> run());
			thread.start();
		}
	}
	
	protected void saveImage(String file) {
		BufferedImage image = new BufferedImage(Settings.saveImageW, Settings.saveImageH, BufferedImage.TYPE_INT_RGB);
		Level.render(((DataBufferInt)(image.getRaster().getDataBuffer())).getData(), Settings.canvasSize, image.getWidth(), image.getHeight());
		Graphics g = image.createGraphics();
		//TODO merge with render method
		g.setColor(Color.WHITE);
		g.drawString("Iterations: " + iterations, 10, 30); 
		g.drawString("Rule: " + Rule.string() + " ("+rule+")", 10, 46);
		if(ant.saveState) {
			g.setColor(Color.red);
			g.drawString("Finding highway... " + ant.minHighwayPeriod, 10, 62);
		} else if(ant.CYCLEFOUND) {
			g.setColor(Color.WHITE);
			g.drawString("Period: " + ant.minHighwayPeriod, 10, 62);
		}
		
		try {
			ImageIO.write(image, "PNG", new File(file+".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean ignoreRules() {	
		if(0==0) return false;
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

	public void render() {
		Graphics g = getBufferStrategy().getDrawGraphics();
		Level.render(pixels, Settings.canvasSize, canvasImage.getWidth(), canvasImage.getHeight());
		
		g.drawImage(canvasImage, 0, 0, 800, 800, null);
		g.setColor(Color.WHITE);
		g.drawString("Iterations: " + iterations, 10, 30); 
		g.drawString("Rule: " + Rule.string() + " ("+rule+")", 10, 46);
		
		if(ant.saveState) {
			g.setColor(Color.red);
			g.drawString("Finding highway... " + ant.minHighwayPeriod, 10, 62);
		} else if(ant.CYCLEFOUND) {
			g.setColor(Color.WHITE);
			g.drawString("Period: " + ant.minHighwayPeriod, 10, 62);
		}
		
		g.dispose();
		getBufferStrategy().show();
	}
}