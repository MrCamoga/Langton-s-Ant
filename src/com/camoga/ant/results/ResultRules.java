package com.camoga.ant.results;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.camoga.ant.ants.AbstractAnt;
import com.camoga.ant.ants.ResultSet;
import com.camoga.ant.net.Client;
import static com.camoga.ant.Main.LOG;
import com.camoga.ant.net.packets.Packet03Result;
import com.camoga.ant.strategies.AssignmentStrategy;

public class ResultRules extends Result {

	protected ByteArrayOutputStream storedrules = new ByteArrayOutputStream();
	protected int count;

	static long lastResultsTime;
	static long DELAY_BETWEEN_RESULTS = 120000;

	public ResultRules(int type) {
		super(type);
		lastResultsTime = System.currentTimeMillis();
		setStrategy(new AssignmentStrategy());
	}

	public void insertAssignments(long rule) {
		if(strategy instanceof AssignmentStrategy st)
			st.insertAssignments(rule);
	}
	
	public synchronized void sendResult() {
		if(!(strategy instanceof AssignmentStrategy)) return; // Only allow to send results from server assignments
		if(System.currentTimeMillis()-lastResultsTime < DELAY_BETWEEN_RESULTS) return;
		try {
			if(storedrules.size() == 0) return;
			Packet03Result packet = new Packet03Result(type,count,storedrules);
			Client.sendPacket(packet);
			storedrules.reset(); // TODO verify server has received the data before deleting (boring)
			count = 0;
			LOG.info("Data sent to server");
		} catch(IOException e) {
			LOG.warning("Could not send rules to server");
		}
		lastResultsTime = System.currentTimeMillis();
	}

	@Override
	public ResultSet initAnt(AbstractAnt ant) {
		Long rule = strategy.next();
		if(rule == null) return null;
		ResultSet result = ant.run(rule,120000000,null);
		this.insertResult(result);
		return result;
	}

	protected synchronized void insertResult(ResultSet result) {
		try {
			if(type > 0) throw new RuntimeException("Types other than 2D not implemented yet");
			// Long[] highway = result.getHighway(); 
			// ByteBuffer bb = ByteBuffer.allocate(28+highway.length*8); // rule, iterations, hash, period, dx, dy, winding, histogram
			// bb.putLong(result.rule);
			// bb.putLong(result.iterations);
			// bb.putLong(result.hash);
			// bb.putInt(highway.length);
			// for(int i = 0; i < highway.length; i++) {
			// 	bb.putLong(highway[i]);
			// }
			Long[] highway = result.getSmallHighway(); 
			ByteBuffer bb = ByteBuffer.allocate(24+highway.length*8); // rule, period, iterations, hash, dx, dy, winding
			bb.putLong(result.rule);
			bb.putLong(highway[0]);
			bb.putLong(result.iterations);
			bb.putLong(result.hash);
			for(int i = 1; i < highway.length; i++) {
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
