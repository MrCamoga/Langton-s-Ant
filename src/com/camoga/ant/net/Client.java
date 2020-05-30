package com.camoga.ant.net;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

import com.camoga.ant.Rule;
import com.camoga.ant.Worker;
import com.camoga.ant.Worker.AntType;
import com.camoga.ant.gui.Window;

public class Client {	
	
	public static final Logger LOG = Logger.getLogger("Client");
	Socket socket;
	static DataOutputStream os;
	static DataInputStream is;
	static String host;
	
	static ArrayList<Worker> workers = new ArrayList<Worker>();
	static int numworkers;
	
	static int ASSIGN_SIZE = 50;
	static long lastResultsTime;
	static long lastAssign;
	static long DELAY_BETWEEN_RESULTS = 120000;
	static int RECONNECT_TIME = 60000;
	static boolean STOP_ON_DISCONNECT;
	
	public static Properties properties;
	
	Thread connectionthread;
	public static boolean logged = false;
	public static String username, password;
	
	public static ArrayList<Long> assignments = new ArrayList<Long>();
	public static ByteArrayOutputStream storedrules = new ByteArrayOutputStream();
	
	public static Client client;
	
	public Client(int numworkers, boolean nolog) throws IOException {
		if(nolog) {
			LOG.setLevel(java.util.logging.Level.OFF);
		} else {
			LOG.setLevel(java.util.logging.Level.INFO);
			System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%n");			
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

		String prop_workers = properties.getProperty("workers");
		if(numworkers == -1) {
			if(prop_workers != null) {
				int n;
				if((n = Integer.parseInt(prop_workers)) > Runtime.getRuntime().availableProcessors()) 
					throw new RuntimeException("Num of workers greater than the number of threads");
				else numworkers = n;				
			} else numworkers = 1;
		}
		Client.numworkers = numworkers;
		
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
	
	private synchronized static void getAssigment() {
		if(!logged) return;
		if(System.currentTimeMillis()-lastAssign < 15000) return;
		lastAssign = System.currentTimeMillis();
		try {
			os.write(PacketType.GETASSIGNMENT.getId());
			os.writeInt(numworkers*ASSIGN_SIZE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void sendAssignmentResult() {
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
	
	
	FileChannel fc;
	private void run() {

//		System.out.println(Runtime.getRuntime().availableProcessors());
//		try {
//			fc = FileChannel.open(new File("langton.properties").toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
//			FileLock lock = fc.tryLock();
//			if(lock == null) {
//				LOG.info("Another instance is running");
//				System.exit(0);
//			}
//		} catch (IOException e) {
//			LOG.info("Another instance is running");
//			System.exit(0);
//		}
//		File file = new File("langton.properties");
//		if(!file.delete()) {
//			LOG.info("Another instance is running");
//			System.exit(0);
//		}
				
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
							
							getAssigment();
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
							assignments.add(bb.getLong());
						}
						LOG.info("New assignment of " + size/2 + " rules!");
						startWorkers(numworkers);
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
	
	private void startWorkers(int num) {
		for(int i = workers.size(); i < num; i++) {
			workers.add(new Worker(i,AntType.NORMAL));
		}
		for(Worker w : workers) {
			w.start();
		}
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
		boolean gui = true;
		boolean nolog = false;
		int numworkers = -1;
		String username = "";
		String password = "";
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
					String param = args[++i];
					if(param.equalsIgnoreCase("max")) {
						numworkers = Runtime.getRuntime().availableProcessors();
					} else numworkers = Integer.parseInt(param);
					break;
				case "-sd":
					STOP_ON_DISCONNECT = true;
					break;
//				case "-u":
//					username = args[i++];
//					char[] pass = System.console().readPassword();
//					break;
				default:
					throw new RuntimeException("Invalid parameters");
				}
		}
		
		client = new Client(numworkers, nolog);
//		if(gui)
			new Window();
	}
	
	public synchronized static long[] getRule() {
		if(assignments.size() < 2*numworkers*ASSIGN_SIZE) {
			getAssigment();
			if(assignments.size() == 0) return new long[] {-1};
		}
		long[] p = new long[] {assignments.remove(0), assignments.remove(0)};
		return p;
	}
	
	public synchronized static void storeRule(long[] rule) {
		try {
			storedrules.write(ByteBuffer.allocate(24).putLong(rule[0]).putLong(rule[1]).putLong(rule[2]).array());
			sendAssignmentResult();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Worker getWorker(int id) {
		if(workers == null || id < 0 || id >= workers.size()) return null;
		return workers.get(id);
	}
}