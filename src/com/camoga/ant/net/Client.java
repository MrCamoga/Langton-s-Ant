package com.camoga.ant.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

import com.camoga.ant.Ant;
import com.camoga.ant.Level;
import com.camoga.ant.Rule;
import com.camoga.ant.Settings;
import com.camoga.ant.Simulation;
import com.camoga.ant.gui.Window;

public class Client {	
	
	public static final Logger LOG = Logger.getLogger("Client");
	Socket socket;
	DataOutputStream os;
	DataInputStream is;
	
	public static Properties properties;
	
	Thread ant;
	Thread connectionthread;
	boolean running = true;
	public boolean antrunning = false;
	public boolean logged = false;
	public static String username;
	
	public ArrayList<long[]> assignments = new ArrayList<long[]>();
	public ByteBuffer results;
	
	public static Client client;
	
	public Client(String host) throws IOException{
		LOG.setLevel(java.util.logging.Level.INFO);
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%n");
		
		properties = new Properties();
		try {
			properties.load(new FileInputStream("langton.properties"));			
		} catch(FileNotFoundException e) {
			new File("langton.properties").createNewFile();
		}
		
		socket = new Socket(host,7357);
		os = new DataOutputStream(socket.getOutputStream());
		is = new DataInputStream(socket.getInputStream());
		
		LOG.info("Connected to server");
		
		if(properties.getProperty("username") != null) {
			LOG.info("Logging in automatically");
			login(properties.getProperty("username"), new BigInteger(Client.properties.getProperty("hash"),16).toString(16));
		}
		
		connectionthread = new Thread(() -> run(), "Client Thread");
		connectionthread.start();
	}
	
	public void register(String username, String hash) {
		if(logged) return;
		
		try {
			os.writeByte(PacketType.REGISTER.getId());
			os.writeByte(hash.length());
			os.write(hash.getBytes());
			os.writeByte(username.length());
			os.write(username.getBytes());
		} catch(IOException e) {
			e.printStackTrace();
		}
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
		if(!logged) return;
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
			
			assignments.remove(0);
			if(assignments.size() > 0)	results = ByteBuffer.allocate(24*assignments.get(0).length);
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
						LOG.info("Logged in as " + username);
						logged = true;
						getAssigment(20);
						getAssigment(20);
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
					if(results == null) {
						results = ByteBuffer.allocate(size*24);
					}
					LOG.info("New assignment of " + size + " rules!");
					assignments.add(rules);
					testRules();
					break;
				case REGISTER:
					int ok = is.readByte();
					if(ok==0) LOG.info("Account registered");
					else if(ok==1) LOG.warning("Username already registered");
					break;
				default:
					break;
				}
			}
		
			socket.close();
		} catch(SocketException e) {
			LOG.warning("Lost connection with the server");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void testRules() {
		if(antrunning) return;
		antrunning = true;
		ant = new Thread(() -> {
			long time;
			while(assignments.size() > 0) {
				for(int i = 0; i < assignments.get(0).length; i++) {
					Ant.init();
					long rule = assignments.get(0)[i];
					Rule.createRule(rule);
					Level.init();
					Simulation.iterations = 0;
					time = System.currentTimeMillis();
					storeRule(Simulation.runRule(rule));
					float seconds = (-time + (time = System.currentTimeMillis()))/1000f;
					LOG.info(rule + "\t" + Rule.string(rule) + "\t " + Simulation.iterations/seconds + " it/s\t" + seconds+ "s");
//					LOG.info((Runtime.getRuntime().totalMemory() -Runtime.getRuntime().freeMemory())/1e6+"MB");
				}
				sendResults();
				getAssigment(20);
				System.gc();
			}
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
		String host = "langtonsant.sytes.net";
		boolean gui = true;
		for(int i = 0; i < args.length; i++) {
			String cmd = args[i];
			if(cmd.startsWith("--")) {
				if(cmd.equalsIgnoreCase("--nogui")) gui = false;
			} else if(cmd.startsWith("-")) {
				String param = args[++i];
				switch(cmd) {
				case "-h":
					host = param;
					break;
				case "-cs":
					Settings.setChunkSize(Integer.parseInt(param));
					break;
				}
			} else {
				throw new RuntimeException("Invalid parameters");
			}
		}
		
		client = new Client(host);
		if(gui) new Window();		
	}
	
	public void storeRule(long[] rule) {
		if(rule[1] > 1) LOG.info(rule[0] + "\t" + Rule.string(rule[0]) + " \t" + rule[1]);
		results.putLong(rule[0]).putLong(rule[1]).putLong(rule[2]);
	}
}
