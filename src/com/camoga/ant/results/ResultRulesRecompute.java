package com.camoga.ant.results;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.camoga.ant.WorkerManager;
import com.camoga.ant.ants.ResultSet;
import com.camoga.ant.net.Client;
import static com.camoga.ant.Main.LOG;
import com.camoga.ant.net.packets.Packet08Result;
import com.camoga.ant.net.packets.Packet09Assignment;

/**
 * Used to recompute rules when new data is to be added on the database. Example: when highways added drift, winding, histograms, etc.
 */
public class ResultRulesRecompute extends ResultRules {

	private long maxrule = 0;

	public ResultRulesRecompute(int type) {
		super(type);
	}

	@Override
	public void insertAssignments(long rule) {
		assignments.add(rule);
		maxrule = Math.max(rule,maxrule);
	}

	@Override
	protected synchronized void getAssignment() {
		if(!Client.logged) return;
		if(WorkerManager.size(type) == 0) return;
		if(System.currentTimeMillis()-lastAssignTime < 15000) return;
		lastAssignTime = System.currentTimeMillis();
		try {
			Packet09Assignment packet = new Packet09Assignment(maxrule);
			Client.sendPacket(packet);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public synchronized void sendResult() {
		try {
			if(storedrules.size() == 0) return;
			Packet08Result packet = new Packet08Result(count,storedrules);
			Client.sendPacket(packet);
			storedrules.reset();
			count = 0;
			LOG.info("Data sent to server");
		} catch(IOException e) {
			LOG.warning("Could not send rules to server");
		}
		lastResultsTime = System.currentTimeMillis();
	}

	@Override
	protected synchronized void insertResult(ResultSet result) {
		try {
			if(type > 0) throw new RuntimeException("Types other than 2D not implemented yet");
			Long[] highway = result.getHighway();
			ByteBuffer bb = ByteBuffer.allocate(12+highway.length*8);
			bb.putLong(result.rule);
			bb.putInt(highway.length);
			for(int i = 0; i < highway.length; i++) {
				bb.putLong(highway[i]);
			}
			storedrules.write(bb.array());
			count++;
			sendResult();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
