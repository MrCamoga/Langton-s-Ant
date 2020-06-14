package com.camoga.ant.net;

import java.awt.GraphicsEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

import com.camoga.ant.WorkerManager;
import com.camoga.ant.gui.Window;

public class Client {	
	
	public static final Logger LOG = Logger.getLogger("Client");
	Socket socket;
	static DataOutputStream os;
	static DataInputStream is;
	static String host;
	
	static int ASSIGN_SIZE = 50;
	static long lastResultsTime;
	volatile static long[] lastAssignTime = new long[2];
	static long DELAY_BETWEEN_RESULTS = 120000;
	static int RECONNECT_TIME = 60000;
	static boolean STOP_ON_DISCONNECT;
	
	public static Properties properties;
	
	Thread connectionthread;
	public static boolean logged = false;
	public static String username, password;
	
	public static ArrayDeque<Long>[] assignments = new ArrayDeque[2];
	public static ByteArrayOutputStream[] storedrules = new ByteArrayOutputStream[2];
	
	public static Client client;
	
	public Client(int normalworkers, int hexworkers, boolean nolog) throws IOException {
		if(nolog) {
			LOG.setLevel(java.util.logging.Level.OFF);
		} else {
			LOG.setLevel(java.util.logging.Level.INFO);
			System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%n");			
		}
		
		properties = new Properties();
		try {
			properties.load(new InputStreamReader(new FileInputStream("langton.properties"),Charset.forName("UTF-8")));			
		} catch(FileNotFoundException e) {
			new File("langton.properties").createNewFile();
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

		assignments[0] = new ArrayDeque<Long>();
		assignments[1] = new ArrayDeque<Long>();
		storedrules[0] = new ByteArrayOutputStream();
		storedrules[1] = new ByteArrayOutputStream();
		WorkerManager.setWorkers(normalworkers, hexworkers);
		
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
	
	private synchronized static void getAssigment(int type) {
		if(!logged) return;
		if(WorkerManager.size(type) == 0) return;
		if(System.currentTimeMillis()-lastAssignTime[type] < 15000) return;
		lastAssignTime[type] = System.currentTimeMillis();
		try {
			if(type == 0) os.write(PacketType.GETASSIGNMENT.getId());
			else os.write(PacketType.GETHEXASSIGN.getId());
			os.writeInt(WorkerManager.size(type)*ASSIGN_SIZE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void sendAssignmentResult() {
		if(System.currentTimeMillis()-lastResultsTime < DELAY_BETWEEN_RESULTS) return;
		boolean datasent = false;
		try {
			if(storedrules[0].size() > 1) {
				os.write(PacketType.SENDRESULTS.getId());
				os.writeInt(storedrules[0].size()/24);
				os.write(storedrules[0].toByteArray());
				storedrules[0].reset();
				datasent = true;
			}
			if(storedrules[1].size() > 1) {				
				os.write(PacketType.SENDHEXRESULTS.getId());
				os.writeInt(storedrules[1].size()/24);
				os.write(storedrules[1].toByteArray());
				storedrules[1].reset();
				datasent = true;
			}
			if(datasent) {
				if(username.equalsIgnoreCase("pazvi")) LOG.info("Sent data to server");
				else LOG.info("Data sent to server");
			}
		} catch(IOException e) {
			LOG.warning("Could not send rules to server");
		}
		
		lastResultsTime = System.currentTimeMillis();
	}
	
	private void run() {	
		lastResultsTime = System.currentTimeMillis();
		while(!STOP_ON_DISCONNECT) {
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

				while(true) {
					switch(PacketType.getPacketType(is.readByte())) {
					case AUTH:
						byte result = is.readByte();
						if(result == 0) {
							username = new String(is.readNBytes(is.readByte()));
							LOG.info("Logged in as " + username);
							logged = true;
							storeCredentials();
							getAssigment(0);
							getAssigment(1);
						} else if(result == 1) {
							Client.username = null;
							Client.password = null;
							LOG.warning("Wrong username or password!");
						}
						break;
					case GETASSIGNMENT:
						int size = is.readInt();
						ByteBuffer bb = ByteBuffer.wrap(is.readNBytes(size*8));
						for(int i = 0; i < size; i++) {
							assignments[0].add(bb.getLong());
						}
						LOG.info("New assignment of " + size/2 + " rules!");
						WorkerManager.start();
						break;
					case GETHEXASSIGN:
						size = is.readInt();
						bb = ByteBuffer.wrap(is.readNBytes(size*8));
						for(int i = 0; i < size; i++) {
							assignments[1].add(bb.getLong());
						}
						LOG.info("New assignment of " + size/2 + " rules!");
						WorkerManager.start();
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
			} catch(EOFException e) {
				LOG.warning("Connection lost");
				logged = false;
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
			
	}
	
	public static void main(String[] args) throws IOException {
		host = "langtonsant.sytes.net";
//		host = "localhost";
		boolean gui = !GraphicsEnvironment.isHeadless();
		boolean nolog = false;
		int normalworkers = 1;
		int hexworkers = 0;
		for(int i = 0; i < args.length; i++) {
			String cmd = args[i];
				switch(cmd) {
				case "--nogui":
					gui = false;
					break;
				case "--host":
					host = args[++i];
					break;
				case "--nolog":
					nolog = true;
					break;
				case "-w":
					normalworkers = Integer.parseInt(args[++i]);
					break;
				case "-wh":
//					hexworkers = Integer.parseInt(args[++i]);
					break;
				case "-sd":
					STOP_ON_DISCONNECT = true;
					break;
				case "-u":
					String username = args[++i];
					System.out.print("Enter password: ");
					String password = hash(System.console().readPassword());
					System.out.println(password);
					if(username != null && password != null) {
						Client.username = username;
						Client.password = password;
					}
					break;
				default:
					throw new RuntimeException("Invalid parameters");
				}
		}
		
		client = new Client(normalworkers,hexworkers,nolog);
		if(gui)
			new Window();
	}
	
	//TODO synchronized removeFirst
	public static long[] getRules(int type, int size) {
		if(assignments[type].size() < 2*size) {
			getAssigment(type);
			return null;
		}
		long[] rules = new long[size*2];
		for(int i = 0; i < size; i++) {
			rules[i] = assignments[type].removeFirst();
		}
		return rules;
	}
	
	public synchronized static long[] getRule(int type) {
		if(assignments[type].size() < 2*WorkerManager.size()*ASSIGN_SIZE) {
			getAssigment(type);
			if(assignments[type].size() == 0) return null;
		}
		long[] p = new long[] {assignments[type].removeFirst(), assignments[type].removeFirst()};
		return p;
	}
	
	public synchronized static void storeRules(int type, long[] rule) {
		try {
			ByteBuffer bb = ByteBuffer.allocate(8*rule.length);
			for(int i = 0; i < rule.length; i++) {
				bb.putLong(rule[i]);
			}
			storedrules[type].write(bb.array());
			sendAssignmentResult();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String hash(char[] chars) {
		byte[] password = null;
		try {
			password = new byte[chars.length];
			for(int i = 0; i < chars.length; i++) {
				password[i] = (byte) chars[i];
			}
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hash = md.digest(password);
			
			String strhash = "";
			for(int i = 0; i < hash.length; i++) {
				strhash += Integer.toHexString(hash[i]&0xff);
			}			
			return strhash;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Arrays.fill(chars, (char) 0);
			Arrays.fill(password, (byte)0);			
		}
		return null;
	}
	
	public static void storeCredentials() {
		if(username != null && password != null) {
			Client.properties.setProperty("username", username);
			Client.properties.setProperty("hash", password);			
		}
		try {
			Client.properties.store(new FileOutputStream("langton.properties"), null);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}