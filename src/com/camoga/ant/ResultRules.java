package com.camoga.ant;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;

import com.camoga.ant.ants.AbstractAnt;
import com.camoga.ant.ants.ResultSet;
import com.camoga.ant.net.Client;
import static com.camoga.ant.Main.LOG;
import com.camoga.ant.net.packets.Packet02Assignment;
import com.camoga.ant.net.packets.Packet03Result;

public class ResultRules extends Result {

    private int type;
	private volatile long lastAssignTime;
	private ArrayDeque<Long> assignments = new ArrayDeque<Long>();
	private ByteArrayOutputStream storedrules = new ByteArrayOutputStream();
	private int count;

	static int ASSIGN_SIZE = 50;
	static long lastResultsTime;
	static long DELAY_BETWEEN_RESULTS = 120000;

    public ResultRules(int type) {
		lastResultsTime = System.currentTimeMillis();
        this.type = type;
    }

    public int getType() { return type; }

	/**
	 * 
	 * @param type
	 * @return {rule, iterations}
	 */
	public synchronized long[] getRule() {
		if(assignments.size() < 2*WorkerManager.size()*ASSIGN_SIZE) {
			getAssignment();
			if(assignments.size() == 0) return null;
		}
		long[] p = new long[] {assignments.removeFirst(), assignments.removeFirst()};
		return p;
	}

	public void insertAssignments(long rule) {
		assignments.add(rule);
	}

    private synchronized void getAssignment() {
		if(!Client.logged) return;
		if(WorkerManager.size(type) == 0) return;
		if(System.currentTimeMillis()-lastAssignTime < 15000) return;
		lastAssignTime = System.currentTimeMillis();
		try {
			Packet02Assignment packet = new Packet02Assignment(type, WorkerManager.size(type)*ASSIGN_SIZE);
			Client.sendPacket(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void sendResult() {
		if(System.currentTimeMillis()-lastResultsTime < DELAY_BETWEEN_RESULTS) return;
		try {
            if(storedrules.size() == 0) return;
			Packet03Result packet = new Packet03Result(type,count,storedrules);
			Client.sendPacket(packet);
			storedrules.reset(); // TODO verify server has received the data before deleting (boring)
			count = 0;
		} catch(IOException e) {
			LOG.warning("Could not send rules to server");
		}
		LOG.info("Data sent to server");
		lastResultsTime = System.currentTimeMillis();
	}

	@Override
	public ResultSet initAnt(AbstractAnt ant) {
		long[] p = getRule();
		if(p == null) return null;
		ResultSet result = ant.run(p[0],p[1],null);
		this.insertResult(result);
		return result;
	}

	private synchronized void insertResult(ResultSet result) {
		try {
			if(type > 0) throw new RuntimeException("Types other than 2D not implemented yet");
			Long[] highway = result.getHighway();
			ByteBuffer bb = ByteBuffer.allocate(28+highway.length*8); // rule, iterations, hash, period, dx, dy, winding, histogram
			bb.putLong(result.rule);
			bb.putLong(result.iterations);
			bb.putLong(result.hash);
			bb.putInt(highway.length);
			for(int i = 0; i < highway.length; i++) {
				bb.putLong(highway[i]);
			}
			storedrules.write(bb.array());
			count++;
			sendResult();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
