package com.camoga.ant.net.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class Packet05Status extends Packet {

	protected int status;
	protected String message;
	
	public Packet05Status(DataInputStream is) throws IOException {
		super(PacketType.STATUS, is);
		readData(is);
	}
	
	public Packet05Status(int statusCode, String message) {
		super(PacketType.STATUS);
		this.status = statusCode;
		this.message = message;
	}

	@Override
	public void writeData(DataOutputStream os) throws IOException {
		super.writeData(os);
		os.writeByte(status);
		os.write(getMessage().getBytes());
		os.write(0);
	}

	@Override
	public void readData(DataInputStream is) throws IOException {
		Scanner sc = new Scanner(is).useDelimiter("\0");
		status = is.readByte();
		message = sc.next();
	}

	public int getStatusCode() { return status; }
	public String getMessage() { return message; }
	public String getFullMessage() { return "Error " + status + ": " + message; }
}