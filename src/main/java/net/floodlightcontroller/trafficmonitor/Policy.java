package net.floodlightcontroller.trafficmonitor;

import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.floodlightcontroller.trafficmonitor.web.PolicySerializer;

@JsonSerialize(using=PolicySerializer.class)
public class Policy {
	public static final String ACTION_NONE = "none";
	public static final String ACTION_DROP = "drop";
	public static final String ACTION_LIMIT = "limit";

	private U64 portSpeedThreshold;
	private String action;
	private long actionDuration;
	private U64 rateLimit;
	
	public Policy(){
		portSpeedThreshold = U64.ZERO;
		action = ACTION_NONE;
		actionDuration = 0;
		rateLimit = U64.ZERO;
	}
	
	public U64 getPortSpeedThreshold() {
		return portSpeedThreshold;
	}
	public void setPortSpeedThreshold(U64 portSpeedThreshold) {
		this.portSpeedThreshold = portSpeedThreshold;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public long getActionDuration() {
		return actionDuration;
	}
	public void setActionDuration(long actionDuration) {
		this.actionDuration = actionDuration;
	}
	public U64 getRateLimit() {
		return rateLimit;
	}
	public void setRateLimit(U64 rateLimit) {
		this.rateLimit = rateLimit;
	}

	
	
}
