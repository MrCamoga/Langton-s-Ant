package com.camoga.ant.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.text.DefaultCaret;

import com.camoga.ant.Ant;
import com.camoga.ant.Level;
import com.camoga.ant.Rule;
import com.camoga.ant.Settings;
import com.camoga.ant.Worker;
import com.camoga.ant.net.Client;

public class Window {
	
	
	Thread thread;
	boolean running;
	public static JFrame f;
	static AntCanvas canvas;
	
	public Window() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		f = new JFrame("Langton's Ant Cellular Automata");
		f.setSize(1250, 800);
		gui(f);
		f.setVisible(true);
		f.setResizable(true);
		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		new Thread(() -> canvas.run(), "Render Thread").start();
	}
	
	class TextAreaOutputStream extends OutputStream {
		private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		private JTextArea t;
		
		public TextAreaOutputStream(JTextArea textarea) {
			super();
			this.t = textarea;
		}

		public void flush() throws IOException {
			t.append(buffer.toString("UTF-8"));
			buffer.reset();
		}
		
		public void write(int b) throws IOException {
			buffer.write(b);
		}
		
		public void write(byte[] b, int off, int len) throws IOException {
			buffer.write(b, off, len);
		}
		
	}
	
	class TextAreaHandler extends StreamHandler {		
		public TextAreaHandler(OutputStream os) {
			setOutputStream(os);
			setFormatter(new SimpleFormatter() {
				public String format(LogRecord record) {
					return String.format("%4$s: %5$s%n", null, null, null, record.getLevel(), record.getMessage(),null);
				}
			});
		}
		
		public void publish(LogRecord record) {
			super.publish(record);
			flush();
		}

		public void flush() {
			super.flush();
		}

		public void close() throws SecurityException {
			
		}
		
	}
	
	class AntCanvas extends Canvas {
		BufferedImage canvasImage = new BufferedImage(Settings.cSIZE*Settings.canvasSize, Settings.cSIZE*Settings.canvasSize, BufferedImage.TYPE_INT_RGB);
		int[] pixels = ((DataBufferInt) canvasImage.getRaster().getDataBuffer()).getData();
		
		public AntCanvas() {}
		
		public void run() {
			createBufferStrategy(3);
			while(true) {
				render();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void render() {
			Graphics g = getBufferStrategy().getDrawGraphics();
			Worker w = Client.getWorker(0);
			if(w==null) return;
			if(w.isRunning()) {
				w.getLevel().render(pixels, Settings.canvasSize, canvasImage.getWidth(), canvasImage.getHeight(), false);				
				g.drawImage(canvasImage, 0, 0, 800, 800, null);
				g.setColor(Color.WHITE);
				g.drawString("Iterations: " + w.getIterations(), 10, 30); 
				g.drawString("Rule: " + Rule.string(w.getRule().rule) + " ("+w.getRule().rule+")", 10, 46);
			}
			
			Ant ant = w.getAnt();
			
			if(ant.saveState) {
				g.setColor(Color.red);
				g.drawString("Finding period... " + ant.minHighwayPeriod, 10, 62);
			} else if(ant.PERIODFOUND) {
				g.setColor(Color.WHITE);
				g.drawString("Period: " + ant.minHighwayPeriod, 10, 62);
			}
			
			g.dispose();
			getBufferStrategy().show();
		}
	}
	
	public void gui(JFrame f) {		
		JMenuBar menu = new JMenuBar();		
			JMenu server = new JMenu("Server");
				ServerActionListener sa = new ServerActionListener();
				JMenuItem connect = new JMenuItem("Connect to Server");
				JMenuItem serversettings = new JMenuItem("Settings");

				connect.addActionListener(sa);
				serversettings.addActionListener(sa);

				server.add(connect);
				server.add(serversettings);
				
		menu.add(server);
				
		canvas = new AntCanvas();
//		c = new Canvas();
//		f.add(c, BorderLayout.CENTER);
		
		JTextArea log = new JTextArea();
		JScrollPane scroll = new JScrollPane(log);
		scroll.setAutoscrolls(true);
		((DefaultCaret)log.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		log.setEditable(false);

		Client.LOG.setUseParentHandlers(false);		
		Client.LOG.addHandler(new TextAreaHandler(new TextAreaOutputStream(log)));
		
		f.add(scroll,BorderLayout.WEST);
		f.add(canvas, BorderLayout.CENTER);
		f.add(menu, BorderLayout.NORTH);
		f.setVisible(true);
		f.pack();
		
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
//		} else if(Ant.PERIODFOUND) {
//			g.setColor(Color.WHITE);
//			g.drawString("Period: " + Ant.minHighwayPeriod, 10, 62);
//		}
//		
//		g.dispose();
//		getBufferStrategy().show();
//	}
}