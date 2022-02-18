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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
	static long DELAY_BETWEEN_RESULTS = 120000;
	static int RECONNECT_TIME = 60000;
	static boolean STOP_ON_DISCONNECT;
	
	public static Properties properties;
	
	Thread connectionthread;
	public static boolean logged = false;
	public static String username, password;
	
	public static final int ANT_TYPES = 4;
	volatile static long[] lastAssignTime = new long[ANT_TYPES];
	public static ArrayDeque<Long>[] assignments = new ArrayDeque[ANT_TYPES];
	public static ByteArrayOutputStream[] storedrules = new ByteArrayOutputStream[ANT_TYPES];
	public static int[] offset = {40,40,48,56};
	public static int[] ruleTypeIDs = {PacketType.GETASSIGN.getId(), PacketType.GETHEXASSIGN.getId(), PacketType.GET3DASSIGN.getId(), PacketType.GET4DASSIGN.getId()};
	
	public static Client client;
	
	public Client(int w, int wh, int w3, int w4, boolean nolog) throws IOException {
		if(nolog) {
			LOG.setLevel(java.util.logging.Level.OFF);
		} else {
			LOG.setLevel(java.util.logging.Level.INFO);
			System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%n");			
		}
		
		properties = new Properties();
		try {
			properties.load(new InputStreamReader(new FileInputStream("langton.properties"),Charset.forName("UTF-8")));	//FIXME sometimes data is erased from langton.properties
		} catch(FileNotFoundException e) {
			new File("langton.properties").createNewFile();
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

		for(int i = 0; i < ANT_TYPES; i++) {
			assignments[i] = new ArrayDeque<Long>();
			storedrules[i] = new ByteArrayOutputStream();			
		}
		//TODO
		WorkerManager.setWorkers(w, wh, w3, w4);
//		WorkerManager.start();
		
//		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//			//save rules that take too much time to compute (>1e10 iterations)
//		}));
		connectionthread = new Thread(() -> run(), "Client Thread");
		connectionthread.start();
	}
	
	public void register(String username, String hash) {
		if(logged) return;
		
		Client.username = username;
		Client.password = hash;
		
		try {
			os.writeByte(PacketType.REGISTER.getId());
			os.writeLong(0x1ddf45c8f51ddb88L);
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
			os.writeLong(0x1ddf45c8f51ddb88L);
			os.writeByte(hash.length());
			os.write(hash.getBytes());
			os.writeByte(username.length());
			os.write(username.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private synchronized static void getAssignment(int type) {
		if(!logged) return;
		if(WorkerManager.size(type) == 0) return;
		if(System.currentTimeMillis()-lastAssignTime[type] < 15000) return;
		lastAssignTime[type] = System.currentTimeMillis();
		try {			
			if(type < ANT_TYPES) os.write(ruleTypeIDs[type]);
			else throw new RuntimeException();
			os.writeInt(WorkerManager.size(type)*ASSIGN_SIZE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void sendAssignmentResult() {
		if(System.currentTimeMillis()-lastResultsTime < DELAY_BETWEEN_RESULTS) return;
		boolean datasent = false;
		try {
			for(int i = 0; i < ANT_TYPES; i++) {
				if(storedrules[i].size() > 1) {
					os.write(ruleTypeIDs[i]+1);
					os.writeInt(storedrules[i].size()/offset[i]);
					os.write(storedrules[i].toByteArray());
					storedrules[i].reset();
					datasent = true;
				}
			}
			if(datasent) {
				LOG.info("Data sent to server");
				lastResultsTime = System.currentTimeMillis();
			}
		} catch(IOException e) {
			LOG.warning("Could not send rules to server");
		}
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
				} else if(System.getenv("LANGTON_USER") != null && System.getenv("LANGTON_PASS") != null) {
					login(System.getenv("LANGTON_USER"), System.getenv("LANGTON_PASS"));
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
							for(int i = 0; i < ANT_TYPES; i++) getAssignment(i);
						} else if(result == 1) {
							Client.username = null;
							Client.password = null;
							LOG.warning("Wrong username or password!");
						}
						break;
					case GETASSIGN:
						int size = is.readInt();
						for(int i = 0; i < size; i++) {
							assignments[0].add(is.readLong());
						}
						LOG.info("New assignment of " + size/2 + " rules");
						WorkerManager.start();
						break;
					case GETHEXASSIGN:
						size = is.readInt();
						for(int i = 0; i < size; i++) {
							assignments[1].add(is.readLong());
						}
						LOG.info("New assignment of " + size/2 + " hex rules");
						WorkerManager.start();
						break;
					case GET3DASSIGN:
						size = is.readInt();
						for(int i = 0; i < size; i++) {
							assignments[2].add(is.readLong());
						}
						LOG.info("New assignment of " + size/2 + " 3d rules");
						WorkerManager.start();
						break;
					case GET4DASSIGN:
						size = is.readInt();
						for(int i = 0; i < size; i++) {
							assignments[3].add(is.readLong());
						}
						LOG.info("New assignment of " + size/2 + " 4d rules");
						WorkerManager.start();
						break;
					case REGISTER:
						int ok = is.readByte();
						if(ok==0) LOG.info("Account registered");
						else if(ok==1) {
							LOG.warning("Username already registered");
							username = null;
							password = null;
						} else {
							LOG.warning("An error has ocurred");
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
		Options options = new Options();
		
		options.addOption(Option.builder("h").longOpt("help").hasArg(false).desc("print help").build());
		options.addOption(Option.builder("ng").longOpt("nogui").hasArg(false).desc("run without GUI").build());                      
		options.addOption(Option.builder("nl").longOpt("nolog").hasArg(false).desc("run without log").build());                      
		options.addOption(Option.builder().longOpt("host").hasArg(true).desc("server address").build());                             
		options.addOption(Option.builder("u").longOpt("user").hasArg(true).desc("login username").build());                          
		options.addOption(Option.builder("w").hasArg(true).desc("number of 2d square grid ants").type(Number.class).build());        
		options.addOption(Option.builder("wh").hasArg(true).desc("number of 2d hexagonal grid ants").type(Number.class).build());    
		options.addOption(Option.builder("w3").hasArg(true).desc("number of 3d ants").type(Number.class).build());                   
		options.addOption(Option.builder("w4").hasArg(true).desc("number of 4d ants").type(Number.class).build());                   
		
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;
		
		try {
			cmd = parser.parse(options, args);

			if(cmd.hasOption("h")) {
				formatter.printHelp("java -jar langton.jar <opt>", options);
				System.exit(0);
			}
			
			int workers2 = cmd.hasOption("w") ? ((Number) cmd.getParsedOptionValue("w")).intValue():0;
			int workershex = cmd.hasOption("wh") ? ((Number) cmd.getParsedOptionValue("wh")).intValue():0;
			int workers3 = cmd.hasOption("w3") ? ((Number) cmd.getParsedOptionValue("w3")).intValue():0;
			int workers4 = cmd.hasOption("w4") ? ((Number) cmd.getParsedOptionValue("w4")).intValue():0;
			boolean nolog = cmd.hasOption("nl");
			if(cmd.hasOption("u")) {
				String username = cmd.getOptionValue("u");
				System.out.print("Enter password: ");
				String password = hash(System.console().readPassword());
				if(username != null && password != null) {
					Client.username = username;
					Client.password = password;
				}
			}
			boolean gui = !GraphicsEnvironment.isHeadless() && !cmd.hasOption("ng");
			host = cmd.hasOption("host") ? cmd.getOptionValue("host"):"langtonsantproject.sytes.net";
			if(workers2 == 0 && workershex == 0 && workers3 == 0 && workers4 == 0) workers2 = 1;
			client = new Client(workers2,workershex,workers3,workers4,nolog);
//			client = new Client(1, 0, 0, 0, false);
			if(gui)
				new Window();
			
		} catch(ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("java -jar langton.jar <opt>", options);
			System.exit(1);
		}
	}
	
	//TODO synchronized removeFirst
	public static long[] getRules(int type, int size) {
		if(assignments[type].size() < 2*size) {
			getAssignment(type);
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
			getAssignment(type);
			if(assignments[type].size() == 0) return null;
		}
		long[] p = new long[] {assignments[type].removeFirst(), assignments[type].removeFirst()};
		return p;
	}
	
	public synchronized static void storeRules(int type, long[] rule) {
		try {
			if(type<2 && rule.length != 5) throw new RuntimeException();
			if(type==2 && rule.length != 6) throw new RuntimeException();
			if(type==3 && rule.length != 7) throw new RuntimeException();
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
