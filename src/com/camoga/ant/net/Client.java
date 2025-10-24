package com.camoga.ant.net;

import java.awt.GraphicsEnvironment;
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
import java.nio.charset.Charset;
import java.util.Properties;

import com.camoga.ant.Worker;
import com.camoga.ant.ResultRules;
import com.camoga.ant.WorkerManager;
import com.camoga.ant.net.packets.Packet.PacketType;
import com.camoga.ant.net.packets.Packet.StatusCodes;
import com.camoga.ant.net.packets.Packet;
import com.camoga.ant.net.packets.Packet00Version;
import com.camoga.ant.net.packets.Packet01Auth;
import com.camoga.ant.net.packets.Packet02Assignment;
import com.camoga.ant.net.packets.Packet04Message;
import com.camoga.ant.net.packets.Packet05Status;

import static com.camoga.ant.Main.LOG;
import static com.camoga.ant.Main.VERSION;

public class Client {
	
	Socket socket;
	private static DataOutputStream os;
	private static DataInputStream is;
	private String host;
	static final int PORT = 7357;
	
	static int RECONNECT_TIME = 60000;
	static boolean STOP_ON_DISCONNECT;
	
	public static Properties properties;
	
	Thread connectionthread;
	public static boolean logged = false;
	public static String username, secrettoken;

	public static Client client;

	public Client(String host, int w2, int wh, int w3, int w4) throws IOException {		
		this.host = host;
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

		WorkerManager.setWorkers(w2, wh, w3, w4);
		LOG.info("Running on " + (w2+wh+w3+w4) + "/" + Runtime.getRuntime().availableProcessors() + " threads");
//		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//			//save rules that take too much time to compute (>1e10 iterations)
//		}));
		connectionthread = new Thread(() -> run(), "Client Thread");
		connectionthread.start();
	}
	
	public void login() {
		if(logged) return;
		if(username != null && secrettoken != null) {}
		else if(System.getenv("LANGTON_USER") != null && System.getenv("LANGTON_PASS") != null) {
			username = System.getenv("LANGTON_USER");
			secrettoken = System.getenv("LANGTON_PASS");
		} else if(properties.getProperty("username") != null && properties.getProperty("secrettoken") != null) {
			username = properties.getProperty("username");
			secrettoken = properties.getProperty("secrettoken");
		} else {
			LOG.warning("You need to login. If you don't have an account, follow the instructions at https://langtonsant.es/downloads.php");
			System.exit(0);
		}
		
		try {
			// Get access token
			String accesstoken;
			URI url = URI.create("https://langtonsant.es/getaccesstoken.php");
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
			sendPacket(versionpacket);
			Packet01Auth loginpacket = new Packet01Auth(username, accesstoken);
			sendPacket(loginpacket);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static synchronized void sendPacket(Packet packet) throws IOException {
		packet.writeData(os);
	}
	
	private void run() {
		while(!STOP_ON_DISCONNECT) {
			try {
				socket = new Socket(host,PORT);

				os = new DataOutputStream(socket.getOutputStream());
				is = new DataInputStream(socket.getInputStream());
				
				LOG.info("Connected to the server");
				
				login();

				while(true) {
					// System.out.println((int)is.readByte()&0xFF);
					// if(0<1)continue;
					PacketType pk = PacketType.getPacketType(is.readByte());
					// System.out.println(pk.getId());
					switch(pk) {
					case AUTH:
						Packet01Auth packetlogin = new Packet01Auth(is);
						username = packetlogin.getUsername();
						LOG.info("Logged in as " + username);
						logged = true;
						// logintries = 0;
						storeCredentials();
						WorkerManager.start();
						break;
					case ASSIGNMENT:
						Packet02Assignment packet = new Packet02Assignment(is);
						for(int i = 0; i < packet.getSize(); i++) {
							((ResultRules)(Worker.workresult)).insertAssignments(is.readLong());
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
						case AUTHDISABLED:
						case INTERNALERROR:
						case ANTDISABLED: // retry login (slow)
							LOG.warning(message);
							break;
						case EXPIREDTOKEN: // retry login (fast)
							LOG.warning(message);
						case BADAUTH:
							// logintries++;
							// if(logintries<5) break;
							break;
						case OUTDATED:
						case UNSETTOKEN:
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
	
	public static void saveProperties() {
		try {
			properties.store(new FileOutputStream("langton.properties"), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void storeCredentials() {
		if(username != null && secrettoken != null) {
			properties.setProperty("username", username);
			properties.setProperty("secrettoken", secrettoken);			
		}
		
		saveProperties();
	}
}