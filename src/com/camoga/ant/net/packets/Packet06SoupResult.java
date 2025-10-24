package com.camoga.ant.net.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map.Entry;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;

import com.camoga.ant.ResultSoup;

public class Packet06SoupResult extends Packet {
	
	ResultSoup result;
	
	/**
	 * 
	 * @param type type of ant
	 * @param size number of rules
	 * @param baos
	 */
	public Packet06SoupResult(ResultSoup result) {
		super(PacketType.SOUPRESULTS);
		this.result = result;
	}

	@Override
	public void writeData(DataOutputStream os) throws IOException {
		super.writeData(os);
		MultiKeyMap<Long, Integer[]> highways = result.getHighways();
		os.writeByte(result.getType());
		os.writeInt(result.getCount());
		os.writeInt(result.getOffset());
		os.writeInt(highways.size());
		os.writeLong(result.getRule());
		os.writeLong(result.getIterations());
		os.writeLong(result.getTotalIterations());
		os.writeUTF(result.getSeedString());
		os.writeInt(result.getPatternSize()); // width
		os.writeInt(result.getPatternSize()); // height
		// os.writeInt(result.getChunkCheck());
		for(Entry<MultiKey<? extends Long>,Integer[]> entry: highways.entrySet()) {
			Long[] highway = entry.getKey().getKeys();
			Integer[] seeds = entry.getValue();
			os.writeInt(highway.length);
			for(int i = 0; i < highway.length; i++)
				os.writeLong(highway[i]);
			os.writeInt(seeds[0]);
			os.writeInt(Math.min(seeds.length-1,seeds[0]));
			// System.out.println("------------");
			// System.out.println(Math.min(seeds.length-1,seeds[0]));
			for(int i = 1; i < seeds.length && seeds[i] != null; i++) {
				os.writeInt(seeds[i]);
				// System.out.println(seeds[i]);
			}
		}
	}

	@Override
	public void readData(DataInputStream is) throws IOException {
		// TODO
	}
}