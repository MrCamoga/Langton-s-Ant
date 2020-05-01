package com.camoga.ant.test.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

import com.camoga.ant.Ant;
import com.camoga.ant.Level;
import com.camoga.ant.Rule;
import com.camoga.ant.Simulation;
import com.camoga.ant.gui.Window;

public class Client {
	
	static final Logger LOG = Logger.getLogger("Client");
	Socket socket;
	DataOutputStream os;
	DataInputStream is;
	
	public static Properties properties;
	
	Thread ant;
	Thread connectionthread;
	boolean running = true;
	boolean antrunning = false;
	public boolean logged = false;
	public static String username;
	
	int ruleindex = 0;
	public ArrayList<long[]> assignments;
	public ByteBuffer results;
	
	public static Client client;
	
	public Client() throws IOException{
		LOG.setLevel(java.util.logging.Level.INFO);
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%n");
		
		properties = new Properties();
		try {
			properties.load(new FileInputStream("langton.properties"));			
		} catch(FileNotFoundException e) {
			new File("langton.properties").createNewFile();
		}
		
		socket = new Socket("langtonsant.sytes.net",7357);
		os = new DataOutputStream(socket.getOutputStream());
		is = new DataInputStream(socket.getInputStream());
		
		LOG.info("Connected to server");
		
		if(properties.getProperty("username") != null) {
			LOG.info("Logging automatically");
			login(properties.getProperty("username"), new BigInteger(Client.properties.getProperty("hash"),16).toString(16));
		}
		
		connectionthread = new Thread(() -> run(), "Client Thread");
		connectionthread.start();
	}
	
	public void login(String username, String hash) {
		if(logged) return;

		try {
			os.writeByte(PacketType.AUTH.getId());
			os.writeByte(hash.length());
			os.write(hash.getBytes());
			os.writeByte(username.length());
			os.write(username.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void getAssigment(int size) {
		if(!logged || antrunning) return;
		try {
			os.write(PacketType.GETASSIGNMENT.getId());
			os.writeInt(size);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendResults() {
		if(results == null) return;
		try {
			os.write(PacketType.SENDRESULTS.getId());
			os.writeInt(assignments.get(0).length);
			os.write(results.array());

			results = ByteBuffer.allocate(24*assignments.get(1).length); //TODO error
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private void run() {
		try {
			while(running) {
				switch(PacketType.getPacketType(is.readByte())) {
				case AUTH:
					byte result = is.readByte();
					if(result == 0) {
						username = new String(is.readNBytes(is.readByte()));
						LOG.info("Logged as " + username);
						logged = true;
					} else if(result == 1) {
						Client.username = null;
						LOG.warning("Wrong username or password!");
					}
					break;
				case GETASSIGNMENT:
					int size = is.readInt();
					ByteBuffer bb = ByteBuffer.wrap(is.readNBytes(size*8));
					long[] rules = new long[size];
					for(int i = 0; i < size; i++) {
						rules[i] = bb.getLong();
					}
					LOG.info("New assignment of " + size + " rules!");
					ruleindex = 0;
					testRules();
					break;
				default:
					break;
				}
			}
		
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void testRules() {
		if(antrunning) return;
		antrunning = true;
		ant = new Thread(() -> {
			for(int i = 0; i < assignments.get(0).length; i++) {
				Ant.init();
				long rule = getRule();
				Rule.createRule(rule);
				Level.init();
				Simulation.iterations = 0;
				LOG.fine(rule + "\t" + Rule.string(rule));
				storeRule(Simulation.runRule(rule));
			}
			sendResults();
			antrunning = false;
			getAssigment(200);
		}, "Langtons Ant");
		ant.start();
		
	}

	public static String toHexString(byte[] data) {
		String result = "";
		for(int i = 0; i < data.length; i++) {
			result += Integer.toHexString(data[i]&0xff);
		}
		return result;
	}
	
	public static void main(String[] args) throws IOException {
		new Window();
		client = new Client();
	}
	
	public synchronized long getRule() {
		return assignments.get(0)[ruleindex++];
	}
	
	public void storeRule(long[] rule) {
		if(rule[1] > 1) LOG.info(rule[0] + "\t" + Rule.string(rule[0]) + " \t" + rule[1]);
		results.putLong(rule[0]).putLong(rule[1]).putLong(rule[2]);
	}
}
