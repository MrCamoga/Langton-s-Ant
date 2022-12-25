package com.camoga.ant.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
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

import com.camoga.ant.Settings;
import com.camoga.ant.Worker;
import com.camoga.ant.WorkerManager;
import com.camoga.ant.net.Client;

public class Window {
	
	Thread thread;
	boolean running;
	public static JFrame f;
	static AntCanvas canvas;
	
	int width = 768, height = 768;
	
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
					return String.format("%5$s%n", null, null, null, record.getLevel(), record.getMessage(),null);
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
		BufferedImage canvasImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int[] pixels = ((DataBufferInt) canvasImage.getRaster().getDataBuffer()).getData();
		
		public AntCanvas() {
			setPreferredSize(new Dimension(640,640));
		}
		
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
			Worker w = WorkerManager.getWorker(0);
			if(w==null) return;

			if(w.isRunning()) {
				w.getLevel().render(canvasImage, pixels, canvasImage.getWidth(), canvasImage.getHeight(), Settings.followAnt || w.getAnt().findingPeriod(), true);				
			}

			g.drawImage(canvasImage, 0, 0, 800, 800, null);
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
				JMenuItem senddata = new JMenuItem("Send Data");
				
				connect.addActionListener(sa);
				serversettings.addActionListener(sa);
				senddata.addActionListener(sa);

				server.add(connect);
				server.add(serversettings);
				server.add(senddata);
				
				
				
		menu.add(server);

		canvas = new AntCanvas();
		
		JTextArea log = new JTextArea(10,60);
		JScrollPane scrollpane = new JScrollPane(log);
		scrollpane.setAutoscrolls(true);
		((DefaultCaret)log.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		log.setEditable(false);

		Client.LOG.setUseParentHandlers(false);		
		Client.LOG.addHandler(new TextAreaHandler(new TextAreaOutputStream(log)));
		
		f.add(scrollpane,BorderLayout.WEST);
		f.add(canvas, BorderLayout.CENTER);
		f.add(menu, BorderLayout.NORTH);
		f.setVisible(true);
		f.pack();
		
	}
}