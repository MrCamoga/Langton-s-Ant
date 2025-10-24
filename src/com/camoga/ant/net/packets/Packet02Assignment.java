package com.camoga.ant.net.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet02Assignment extends Packet {

	protected int type, size;
	
	public Packet02Assignment(DataInputStream is) throws IOException {
		super(PacketType.ASSIGNMENT); // TODO put readData on parent class
		readData(is);
	}
	
	public Packet02Assignment(int type, int size) {
		super(PacketType.ASSIGNMENT);
		this.type = type;
		this.size = size;
	}
	
	@Override
	public void writeData(DataOutputStream os) throws IOException {
		super.writeData(os);
		os.writeByte(type);
		os.writeInt(size);
	}

	@Override
	public void readData(DataInputStream is) throws IOException {
		type = is.readByte();
		size = is.readInt();
	}

	public int getType() { return type; }
	public int getSize() { return size; }
}