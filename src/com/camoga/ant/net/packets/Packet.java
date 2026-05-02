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
		MESSAGE(4), STATUS(5), 
		SOUPRESULTS(6),
		NEWRESULTS(8),
		NEWASSIGNMENT(9),
		DISCONNECT(7);
		
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
		INVALID(-1, "Invalid packet"),
		OUTDATED(0, "Client version outdated"),
		EXPIREDTOKEN(1, "Expired access token"),
		BADAUTH(2, "Wrong credentials"),
		UNSETTOKEN(3, "Missing secret token"),
		LOGGED(4, "Login successful"),
		NEWVERSION(5, "New version available"),
		BADREQUEST(6, "Bad packet"),
		UNAUTHORIZED(7, "Unauthorized"),
		RATELIMIT(8, "Rate limit exceeded"),
		INTERNALERROR(64, "Internal server error"),
		AUTHDISABLED(65, "Login disabled. Server under maintenance"),
		ANTDISABLED(66, "Work type disabled"),
		;
		
		protected int code;
		protected String message;
		
		StatusCodes(int code, String message) {
			this.code = code;
			this.message = message;
		}
		
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