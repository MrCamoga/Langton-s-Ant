package com.camoga.ant.net.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Packet00Version extends Packet {

	protected int[] version = new int[3];
	
	public Packet00Version(int[] version) {
		super(PacketType.VERSION);
		for(int i = 0; i < version.length; i++) this.version[i] = version[i];
	}

	@Override
	public void writeData(DataOutputStream os) throws IOException {
		super.writeData(os);
		for(int v : version) os.writeInt(v);
	}

	@Override
	public void readData(DataInputStream is) throws IOException {
		for(int i = 0; i < version.length; i++) is.readInt();
	}

	public int getMajor() { return version[0]; }
	public int getMinor() { return version[1]; }
	public int getPatch() { return version[2]; }
	
	public String getVersion() { 
		return Arrays.stream(version).mapToObj(String::valueOf).collect(Collectors.joining("."));
	}
	
}