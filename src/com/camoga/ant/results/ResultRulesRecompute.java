package com.camoga.ant.results;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.camoga.ant.ants.ResultSet;
import com.camoga.ant.net.Client;
import static com.camoga.ant.Main.LOG;
import com.camoga.ant.net.packets.Packet08Result;
import com.camoga.ant.strategies.AssignmentRecomputeStrategy;
import com.camoga.ant.strategies.StrategyInterface;

/**
 * Used to recompute rules when new data is to be added on the database. Example: when highways added drift, winding, histograms, etc.
 */
public class ResultRulesRecompute extends ResultRules {
	public ResultRulesRecompute(int type) {
		super(type);
		setStrategy(new AssignmentRecomputeStrategy());
	}
	
	@Override
	public synchronized void sendResult() {
		if(!(strategy instanceof AssignmentRecomputeStrategy)) return;
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

	@Override
	protected StrategyInterface defaultStrategy() {
		return new AssignmentRecomputeStrategy();
	}
}
