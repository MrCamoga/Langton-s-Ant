package com.camoga.ant.net.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Packet {
	
	public enum PacketType {
//		INVALID(-1),
//		AUTH(0),
//		GETASSIGN(1),SENDRESULTS(2),
//		MESSAGE(4),	
//		GETHEXASSIGN(5),SENDHEXRESULTS(6),
//		ERRORMESSAGE(7),
//		GET3DASSIGN(8), SEND3DRESULTS(9),
//		GET4DASSIGN(10),SEND4DRESULTS(11);
		INVALID(-1),
		VERSION(0), AUTH(1),
		ASSIGNMENT(2), RESULTS(3),
		MESSAGE(4), STATUS(5);
		
		private int id;
		
		PacketType(int id) { this.id = id; }
		
		public int getId() { return id; }

		public static PacketType getPacketType(int id) {
			for(PacketType p : PacketType.values()) {
				if(p.getId()==id) return p;
			}
			return PacketType.INVALID;
		}
	}
	
	public enum StatusCodes {
		INVALID(-1),
		OUTDATED(0), // client version is outdated
		EXPIREDTOKEN(1), // user access token has expired
		BADTOKEN(2), // user access token is wrong
		UNSETTOKEN(3), // user secret token is not set
		LOGGED(4), // user has successfully logged in
		NEWVERSION(5), // there is a new version available
		INTERNALERROR(64), // general server error
		AUTHDISABLED(65) //	login disabled for maintenance
		;
		
		protected int code;
		
		StatusCodes(int code) { this.code = code; }
		
		public static StatusCodes getStatus(int code) {
			for(StatusCodes p : StatusCodes.values()) {
				if(p.getCode()==code) return p;
			}
			return StatusCodes.INVALID;
		}
		
		public int getCode() { return code; }
	}
	
	protected int id;
	protected DataInputStream is;
	
	
	public Packet(PacketType type) {
		this.id = type.getId();
	}
	
	public Packet(PacketType type, DataInputStream is) throws IOException {
		this.id = type.getId();
		this.is = is;
	}
	
	public void writeData(DataOutputStream os) throws IOException {
		os.writeByte(id);
	}
	
	public abstract void readData(DataInputStream is) throws IOException;
	
	public int getId() {
		return id;
	}
}