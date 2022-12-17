package com.camoga.ant.net.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class Packet02Assignment extends Packet {

	protected int type, size, count;
	
	public Packet02Assignment(DataInputStream is) throws IOException {
		super(PacketType.ASSIGNMENT,is); // TODO put readData on parent class
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
	public Iterator<Long> getData() {
		if(is == null) return null;
		Iterator<Long> it = new Iterator<Long>() {
			public boolean hasNext() {
				return count<size;
			}

			@Override
			public Long next() {
				try {
					count++;
					return is.readLong();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		return it;
	}
}