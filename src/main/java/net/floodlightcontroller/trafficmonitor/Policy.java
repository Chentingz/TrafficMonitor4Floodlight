package net.floodlightcontroller.trafficmonitor;

import org.projectfloodlight.openflow.types.U64;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.floodlightcontroller.trafficmonitor.web.PolicySerializer;

@JsonSerialize(using=PolicySerializer.class)
public class Policy {
	public static final String ACTION_DROP = "drop";
	public static final String ACTION_LIMIT = "limit";

	private U64 trafficThreshold;
	private String action;
	private long actionDuration;
	private U64 rateLimit;
	
	public Policy(){
		trafficThreshold = U64.of(1000);
		action = ACTION_DROP;				
		actionDuration = 30;
		rateLimit = U64.ZERO;
	}
	public Policy(Policy policy){
		this.trafficThreshold = policy.getTrafficThreshold();
		this.action = policy.getAction();				
		this.actionDuration = policy.getActionDuration();
		this.rateLimit = policy.getRateLimit();
	}
	
	public U64 getTrafficThreshold() {
		return trafficThreshold;
	}
	public void setTrafficThreshold(U64 trafficThreshold) {
		this.trafficThreshold = trafficThreshold;
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
