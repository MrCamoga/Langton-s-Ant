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
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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

import com.camoga.ant.Version;
import com.camoga.ant.WorkerManager;
import com.camoga.ant.gui.Window;
import com.camoga.ant.net.packets.Packet.PacketType;
import com.camoga.ant.net.packets.Packet.StatusCodes;
import com.camoga.ant.net.packets.Packet00Version;
import com.camoga.ant.net.packets.Packet01Auth;
import com.camoga.ant.net.packets.Packet02Assignment;
import com.camoga.ant.net.packets.Packet03Result;
import com.camoga.ant.net.packets.Packet04Message;
import com.camoga.ant.net.packets.Packet05Status;

public class Client {
	
	public static final Logger LOG = Logger.getLogger("Client");
	Socket socket;
	static DataOutputStream os;
	static DataInputStream is;
	static String host;
	static final int PORT = 7357;
	static final Version VERSION = new Version(1,0,0);
	
	static int ASSIGN_SIZE = 50;
	static long lastResultsTime;
	static long DELAY_BETWEEN_RESULTS = 120000;
	static int RECONNECT_TIME = 60000;
	static boolean STOP_ON_DISCONNECT;
	
	public static Properties properties;
	
	Thread connectionthread;
	public static boolean logged = false;
	public static String username, secrettoken;
	
	public static final int ANT_TYPES = 4;
	volatile static long[] lastAssignTime = new long[ANT_TYPES];
	public static ArrayDeque<Long>[] assignments = new ArrayDeque[ANT_TYPES];
	public static ByteArrayOutputStream[] storedrules = new ByteArrayOutputStream[ANT_TYPES];
	public static int[] offset = {48,40,48,56};

	public static Client client;
	
	public Client(int w, int wh, int w3, int w4, boolean nolog) throws IOException {
		if(nolog) {
			LOG.setLevel(java.util.logging.Level.WARNING);
		} else {
			LOG.setLevel(java.util.logging.Level.INFO);
			System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%n");			
		}
		LOG.info("Langton's Ant Client v"+VERSION);
		
		properties = new Properties();
		try {
			LOG.info("Reading config...");
			properties.load(new InputStreamReader(new FileInputStream("langton.properties"),Charset.forName("UTF-8")));
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

		WorkerManager.setWorkers(w, wh, w3, w4);
		LOG.info("Running on " + (w+wh+w3+w4) + " threads");
//		WorkerManager.start();
//		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//			//save rules that take too much time to compute (>1e10 iterations)
//		}));
		connectionthread = new Thread(() -> run(), "Client Thread");
		connectionthread.start();
	}
	
	private int logintries = 0;
	public void login() {
		if(logged) return;
		if(username != null && secrettoken != null) {}
		else if(System.getenv("LANGTON_USER") != null && System.getenv("LANGTON_PASS") != null) {
			Client.username = System.getenv("LANGTON_USER");
			Client.secrettoken = System.getenv("LANGTON_PASS");
		} else if(properties.getProperty("username") != null && properties.getProperty("secrettoken") != null) {
			Client.username = properties.getProperty("username");
			Client.secrettoken = properties.getProperty("secrettoken");
		} else {
			LOG.warning("You have to login.");
			System.exit(0);
		}
		
		try {
			// Get access token
			String accesstoken;
			URI url = URI.create("https://langtonsantproject.sytes.net/getaccesstoken.php");
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest req = HttpRequest.newBuilder()
					.uri(url)
					.setHeader("Content-Type", "application/x-www-form-urlencoded")
					.POST(HttpRequest.BodyPublishers.ofString(String.format("username=%s&pass=%s",username,secrettoken)))
					.build();
			HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
			int status = response.statusCode();
			if(status != 200) {
				LOG.warning(response.body());
				LOG.warning("An error ocurred while getting access token. Error code: " + response.headers().firstValue("status").orElse(status+""));
				if(status==401) System.exit(1);
				return;
			} else {
				accesstoken = response.body();
			}
			
			Packet00Version versionpacket = new Packet00Version(VERSION);
			versionpacket.writeData(os);
			Packet01Auth loginpacket = new Packet01Auth(username, accesstoken);
			loginpacket.writeData(os);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private synchronized static void getAssignment(int type) {
		if(!logged) return;
		if(WorkerManager.size(type) == 0) return;
		if(System.currentTimeMillis()-lastAssignTime[type] < 15000) return;
		lastAssignTime[type] = System.currentTimeMillis();
		try {
			Packet02Assignment packet = new Packet02Assignment(type, WorkerManager.size(type)*ASSIGN_SIZE);
			packet.writeData(os);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized static void sendAssignmentResult() {
		if(System.currentTimeMillis()-lastResultsTime < DELAY_BETWEEN_RESULTS) return;
		boolean datasent = false;
		try {
			for(int i = 0; i < ANT_TYPES; i++) {
				if(storedrules[i].size() > 1) {
					Packet03Result packet = new Packet03Result(i,storedrules[i].size()/offset[i],storedrules[i]);
					packet.writeData(os);
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
				socket = new Socket(host,PORT);
				os = new DataOutputStream(socket.getOutputStream());
				is = new DataInputStream(socket.getInputStream());
				
				LOG.info("Connected to server");
				
				login();

				while(true) {
					PacketType pk = PacketType.getPacketType(is.readByte());
					switch(pk) {
					case AUTH:
						Packet01Auth packetlogin = new Packet01Auth(is);
						username = packetlogin.getUsername();
						LOG.info("Logged in as " + username);
						logged = true;
						logintries = 0;
						storeCredentials();
						for(int i = 0; i < ANT_TYPES; i++) getAssignment(i);
						break;
					case ASSIGNMENT:
						Packet02Assignment packet = new Packet02Assignment(is);
						for(int i = 0; i < packet.getSize(); i++) {
							assignments[packet.getType()].add(is.readLong());
						}
						LOG.info("New assignment of " + packet.getSize()/2 + " rules");
						WorkerManager.start();
						break;
					case RESULTS: // check if successful and delete data, else retry or store data in file for future retry
						break;
					case MESSAGE:
						Packet04Message packetmessage = new Packet04Message(is);
						LOG.info(packetmessage.getMessage());
						break;
					case STATUS:
						Packet05Status packetstatus = new Packet05Status(is);
						int status = packetstatus.getStatusCode();
						String message = packetstatus.getFullMessage();
						switch(StatusCodes.getStatus(status)) {
						case AUTHDISABLED, INTERNALERROR, ANTDISABLED: // retry login (slow)
							LOG.warning(message);
							break;
						case EXPIREDTOKEN: // retry login (fast)
							LOG.warning(message);
						case BADAUTH:
							logintries++;
							if(logintries<5) break;
						case OUTDATED, UNSETTOKEN:
							LOG.warning(message);
							System.exit(1);
							break;
//						case INVALID:
//							LOG.warning(message);
//							break;
//						case UNAUTHORIZED:
//							LOG.warning(message);
//							break;
						default:
							LOG.warning(message);
							break;
						}
						break;
					default:
						break;
					}
				}
				
			} catch(UnknownHostException | SocketException e) {
				LOG.info("Could not connect to the server");
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
				System.out.print("Enter secret token: ");
				String secrettoken = new String(System.console().readPassword());
				if(username != null && secrettoken != null) {
					Client.username = username;
					Client.secrettoken = secrettoken;
				}
			}
			boolean gui = !GraphicsEnvironment.isHeadless() && !cmd.hasOption("ng");
			host = cmd.hasOption("host") ? cmd.getOptionValue("host"):"langtonsantproject.sytes.net";
			if(workers2 == 0 && workershex == 0 && workers3 == 0 && workers4 == 0) workers2 = 1;
			client = new Client(workers2,workershex,workers3,workers4,nolog);
			if(gui)
				new Window();
			
		} catch(ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("java -jar langton.jar <opt>", options);
			System.exit(1);
		}
	}

	/**
	 * 
	 * @param type
	 * @return {rule, iterations}
	 */
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
			if(type==0 && rule.length != 6) throw new RuntimeException();
			if(type==1 && rule.length != 5) throw new RuntimeException();
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
	
	public static void saveProperties() {
		try {
			Client.properties.store(new FileOutputStream("langton.properties"), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void storeCredentials() {
		if(username != null && secrettoken != null) {
			Client.properties.setProperty("username", username);
			Client.properties.setProperty("secrettoken", secrettoken);			
		}
		
		saveProperties();
	}
}
