package com.camoga.ant.net.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet03Result extends Packet {

	protected int type, size;
	protected ByteArrayOutputStream rules;
	
	public Packet03Result(int type, int size, ByteArrayOutputStream baos) {
		super(PacketType.RESULTS);
		this.type = type;
		this.size = size;
		this.rules = baos;
	}

	@Override
	public void writeData(DataOutputStream os) throws IOException {
		super.writeData(os);
		os.writeByte(type);
		os.writeInt(size);
		rules.writeTo(os);
	}

	@Override
	public void readData(DataInputStream is) throws IOException {
		// TODO
	}
	
	public int getType() { return type; }
	public int getSize() { return size; }
}