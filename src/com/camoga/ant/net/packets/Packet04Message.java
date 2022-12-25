package com.camoga.ant.net.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
		os.writeInt(message.length());
		os.write(message.getBytes());
	}

	@Override
	public void readData(DataInputStream is) throws IOException {
		message = new String(is.readNBytes(is.readInt()));
	}
	
	public String getMessage() { return message; }
}