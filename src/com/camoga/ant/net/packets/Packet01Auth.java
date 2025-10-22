package com.camoga.ant.net.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class Packet01Auth extends Packet {

	protected String username, accesstoken;
	
	public Packet01Auth(String username, String accesstoken) {
		super(PacketType.AUTH);
		this.username = username;
		this.accesstoken = accesstoken;
	}
	
	public Packet01Auth(DataInputStream is) throws IOException {
		super(PacketType.AUTH, is);
		readData(is);
	}

	@Override
	public void writeData(DataOutputStream os) throws IOException {
		super.writeData(os);
		os.writeUTF(username);
		os.writeUTF(accesstoken);
	}

	@Override
	public void readData(DataInputStream is) throws IOException {
		username = is.readUTF();
	}

	public String getUsername() { return username; }
	public String getAccessToken() { return accesstoken; }
}