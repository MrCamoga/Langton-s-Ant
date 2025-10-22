package com.camoga.ant;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;

import com.camoga.ant.net.Client;
import static com.camoga.ant.net.Client.LOG;
import com.camoga.ant.net.packets.Packet02Assignment;
import com.camoga.ant.net.packets.Packet03Result;

public class ResultRules extends Result {

    private int type;
	private volatile long lastAssignTime;
	private ArrayDeque<Long> assignments = new ArrayDeque<Long>();
	private ByteArrayOutputStream storedrules = new ByteArrayOutputStream();
	private int offset;

	static int ASSIGN_SIZE = 50;
	static long lastResultsTime;
	static long DELAY_BETWEEN_RESULTS = 120000;
	public static final int ANT_TYPES = 4;

    public ResultRules(int type) {
        this.type = type;
        this.offset = new int[]{56,40,48,56}[type];
    }

    public int getType() { return type; }

	/**
	 * 
	 * @param type
	 * @return {rule, iterations}
	 */
	public synchronized long[] getRule() {
		if(assignments.size() < 2*WorkerManager.size()*ASSIGN_SIZE) {
			getAssignment(type);
			if(assignments.size() == 0) return null;
		}
		long[] p = new long[] {assignments.removeFirst(), assignments.removeFirst()};
		return p;
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
		boolean datasent = false;
		try {
            if(storedrules.size() > 1) {
                Packet03Result packet = new Packet03Result(type,storedrules.size()/offset,storedrules);
                Client.sendPacket(packet);
                storedrules.reset(); // TODO verify server has received the data before deleting (boring)
                datasent = true;
            }
		} catch(IOException e) {
			LOG.warning("Could not send rules to server");
		}
		if(datasent) {
			LOG.info("Data sent to server");
			lastResultsTime = System.currentTimeMillis();
		}
	}
}
