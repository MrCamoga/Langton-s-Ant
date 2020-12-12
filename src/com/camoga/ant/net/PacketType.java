package com.camoga.ant.net;

public enum PacketType {
	INVALID(-1),
	AUTH(0),REGISTER(3),
	GETASSIGN(1),SENDRESULTS(2),
	MESSAGE(4),	
	GETHEXASSIGN(5),SENDHEXRESULTS(6),
	ERRORMESSAGE(7),
	GET3DASSIGN(8), SEND3DRESULTS(9),
	GET4DASSIGN(10),SEND4DRESULTS(11);
	
	private int id;
	
	PacketType(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	

	
	public static PacketType getPacketType(int id) {
		for(PacketType p : PacketType.values()) {
			if(p.getId()==id) return p;
		}
		return PacketType.INVALID;
	}
}