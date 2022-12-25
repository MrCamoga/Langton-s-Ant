package com.camoga.ant.net.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.camoga.ant.Version;

public class Packet00Version extends Packet {

	protected Version version;
	
	public Packet00Version(Version version) {
		super(PacketType.VERSION);
		this.version = version;
	}

	@Override
	public void writeData(DataOutputStream os) throws IOException {
		super.writeData(os);
		os.writeInt(version.getMajor());
		os.writeInt(version.getMinor());
		os.writeInt(version.getPatch());
	}

	@Override
	public void readData(DataInputStream is) throws IOException {
		version = new Version(is.readInt(),is.readInt(),is.readInt());
	}

	public Version getVersion() { 
		return version;
	}
}