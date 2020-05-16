package com.camoga.ant.net;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
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
	static String host;
	
	static int worktype = 0;
	static int ASSIGN_SIZE = 50;
	static long lastResultsTime;
	static long DELAY_BETWEEN_RESULTS = 120000;
	static int RECONNECT_TIME = 60000;
	
	public static Properties properties;
	
	Thread ant;
	Thread connectionthread;
	boolean running = true;
	public boolean antrunning = false;
	public boolean logged = false;
	public static String username, password;
	public boolean tryToReconnect = true;
	
	public ArrayList<long[]> assignments = new ArrayList<long[]>();
	public ByteArrayOutputStream storedrules = new ByteArrayOutputStream();
	
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
		
		connectionthread = new Thread(() -> run(), "Client Thread");
		connectionthread.start();
	}
	
	public void register(String username, String hash) {
		if(logged) return;
		
		Client.username = username;
		Client.password = hash;
		
		
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
		Client.username = username;
		Client.password = hash;

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
	
	public void sendAssignmentResult() {
		if(System.currentTimeMillis()-lastResultsTime < DELAY_BETWEEN_RESULTS) return;
		
		try {
			os.write(PacketType.SENDRESULTS.getId());
			os.writeInt(storedrules.size()/24);
			os.write(storedrules.toByteArray());
			storedrules.reset();
			LOG.info("Data sent to server");
		} catch(IOException e) {
			LOG.warning("Could not send rules to server");
		}
		
		lastResultsTime = System.currentTimeMillis();
	}
	
	
	private void run() {
		lastResultsTime = System.currentTimeMillis()-DELAY_BETWEEN_RESULTS;
		while(tryToReconnect) {
			try {
				socket = new Socket(host,7357);
				os = new DataOutputStream(socket.getOutputStream());
				is = new DataInputStream(socket.getInputStream());
				
				LOG.info("Connected to server");
				
				if(username != null && password != null) {
					login(username,password);
				} else if(properties.getProperty("username") != null && properties.getProperty("hash") != null) {
					login(properties.getProperty("username"), new BigInteger(Client.properties.getProperty("hash"),16).toString(16));
				}
			
				while(running) {
					switch(PacketType.getPacketType(is.readByte())) {
					case AUTH:
						byte result = is.readByte();
						if(result == 0) {
							byte[] buffer = new byte[is.readByte()];
							is.read(buffer);
							username = new String(buffer);
							LOG.info("Logged in as " + username);
							logged = true;
							
							getAssigment(ASSIGN_SIZE);
							getAssigment(ASSIGN_SIZE);
						} else if(result == 1) {
							Client.username = null;
							Client.password = null;
							LOG.warning("Wrong username or password!");
						}
						break;
					case GETASSIGNMENT:
						int size = is.readInt();
						byte[] buffer = new byte[size*8];
						is.read(buffer);
						ByteBuffer bb = ByteBuffer.wrap(buffer);
						long[] rules = new long[size];
						for(int i = 0; i < size; i++) {
							rules[i] = bb.getLong();
						}
						LOG.info("New assignment of " + size/2 + " rules!");
						assignments.add(rules);
						testRules();
						break;
					case REGISTER:
						int ok = is.readByte();
						if(ok==0) LOG.info("Account registered");
						else if(ok==1) {
							LOG.warning("Username already registered");
							username = null;
							password = null;
						}
						break;
					default:
						break;
					}
				}
				
			} catch(UnknownHostException | SocketException e) {
				LOG.warning("Could not connect to the server");
				logged = false;
				try {
					Thread.sleep(RECONNECT_TIME);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
			
	}
	
	public synchronized void testRules() {
		if(antrunning) return;
		antrunning = true;
		ant = new Thread(() -> {
			long time;
			while(assignments.size() > 0) {
				for(int i = 0; i < assignments.get(0).length/2; i++) {

					long rule = assignments.get(0)[2*i];
					long iterations = assignments.get(0)[2*i+1];
					
					Level.init();
					Ant.init(iterations);
					Rule.createRule(rule);
					Simulation.iterations = 0;
					time = System.currentTimeMillis();
					storeRule(Simulation.runRule(rule,iterations));
					float seconds = (-time + (time = System.currentTimeMillis()))/1000f;
					LOG.info(rule + "\t" + Rule.string(rule) + "\t " + Simulation.iterations/seconds + " it/s\t" + seconds+ "s");
//					LOG.info((Runtime.getRuntime().totalMemory() -Runtime.getRuntime().freeMemory())/1e6+"MB");
				}
				assignments.remove(0);
				sendAssignmentResult();
				getAssigment(ASSIGN_SIZE);
				System.gc();
			}
			antrunning = false;
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
		host = "langtonsant.sytes.net";
		worktype = 0;
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
				case "-w":
					int work = Integer.parseInt(param);
					if(work < 0 || work > 2) throw new RuntimeException("Illegal work type");
					worktype = work;
				}
			} else {
				throw new RuntimeException("Invalid parameters");
			}
		}
		
		client = new Client();
		if(gui)
			new Window();
	}
	
	public void storeRule(long[] rule) {
		if(rule[1] > 1) LOG.info(rule[0] + "\t" + Rule.string(rule[0]) + " \t" + rule[1]);
		try {
			storedrules.write(ByteBuffer.allocate(24).putLong(rule[0]).putLong(rule[1]).putLong(rule[2]).array());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
