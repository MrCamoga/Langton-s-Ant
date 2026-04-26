package com.camoga.ant.net.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet09Assignment extends Packet {

	protected int size;
	protected long minrule;
	
	public Packet09Assignment(DataInputStream is) throws IOException {
		super(PacketType.NEWASSIGNMENT);
		readData(is);
	}
	
	public Packet09Assignment(long minrule) {
		super(PacketType.NEWASSIGNMENT);
		this.minrule = minrule;
	}
	
	@Override
	public void writeData(DataOutputStream os) throws IOException {
		super.writeData(os);
		os.writeLong(minrule);
	}

	@Override
	public void readData(DataInputStream is) throws IOException {
		size = is.readInt();
	}

	public int getSize() { return size; }
}