package com.camoga.ant.strategies;

import java.io.IOException;
import java.util.ArrayDeque;

import com.camoga.ant.net.Client;
import com.camoga.ant.net.packets.Packet;
import com.camoga.ant.net.packets.Packet02Assignment;
import com.camoga.ant.results.Result;

public class AssignmentStrategy implements StrategyInterface {

	protected ArrayDeque<Long> assignments = new ArrayDeque<Long>();
	protected Result result;
	static int ASSIGN_SIZE = 50;
	protected long lastAssignTime;

	@Override
	public synchronized Long next() {
		if(assignments.size() < 2*result.getWorkerCount()*ASSIGN_SIZE) {
			getAssignment();
			if(assignments.size() == 0) return null;
		}
		return assignments.removeFirst();
	}

	protected synchronized void getAssignment() {
		if(!Client.logged) return;
		if(result.getWorkerCount() == 0) return;
		if(System.currentTimeMillis()-lastAssignTime < 15000) return;
		lastAssignTime = System.currentTimeMillis();
		try {
			Client.sendPacket(createAssignmentPacket());
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	protected Packet createAssignmentPacket() {
		return new Packet02Assignment(result.getType(), result.getWorkerCount()*ASSIGN_SIZE);
	}

	@Override
	public void init(Result result) {
		this.result = result;
	}

	public void insertAssignments(long rule) {
		assignments.add(rule);
	}

	@Override
	public int remaining() {
		return assignments.size();
	}
}