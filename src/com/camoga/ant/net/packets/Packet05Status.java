package com.camoga.ant.net.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.camoga.ant.net.packets.Packet.PacketType;

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
		os.writeInt(getMessage().length());
		os.write(getMessage().getBytes());
	}

	@Override
	public void readData(DataInputStream is) throws IOException {
		status = is.readByte();
		message = new String(is.readNBytes(is.readInt()));
	}

	public int getStatusCode() { return status; }
	public String getMessage() { return message; }
	public String getFullMessage() { return "Error " + status + ": " + message; }
}