package com.camoga.ant.strategies;

import com.camoga.ant.net.packets.Packet;
import com.camoga.ant.net.packets.Packet09Assignment;

public class AssignmentRecomputeStrategy extends AssignmentStrategy {
	protected long maxrule = 0;

	@Override
	protected Packet createAssignmentPacket() {
		return new Packet09Assignment(maxrule);
	}

	@Override
	public void insertAssignments(long rule) {
		super.insertAssignments(rule);
		maxrule = Math.max(rule,maxrule);
	}
}
