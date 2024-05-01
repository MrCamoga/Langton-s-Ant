package com.camoga.ant.net.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class Packet04Message extends Packet {

	protected String message;
	
	public Packet04Message(DataInputStream is) throws IOException {
		super(PacketType.MESSAGE, is);
		readData(is);
	}
	
	public Packet04Message(String message) {
		super(PacketType.MESSAGE);
		this.message = message;
	}

	@Override
	public void writeData(DataOutputStream os) throws IOException {
		super.writeData(os);
		os.write(message.getBytes());
		os.write(0);
	}

	@Override
	public void readData(DataInputStream is) throws IOException {
		Scanner sc = new Scanner(is).useDelimiter("\0");
		message = sc.next();
	}
	
	public String getMessage() { return message; }
}