package net.floodlightcontroller.trafficmonitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import net.floodlightcontroller.core.types.NodePortTuple;

import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TrafficAnalyzer {
	private static final Logger logger = LoggerFactory.getLogger(TrafficAnalyzer.class);
	private static U64 threashold = U64.of(100);		// „–÷µ£¨µ•ŒªBps
	
	public static void Analysis(HashMap<NodePortTuple, SwitchPortStatistics> portStatsMap, HashSet<NodePortTuple> abnormalTrafficSet){
		U64 portSpeed = U64.ZERO;
		U64 rxSpeed = U64.ZERO;
		U64 txSpeed = U64.ZERO;
		for(Entry<NodePortTuple, SwitchPortStatistics> e : portStatsMap.entrySet()){
			rxSpeed = e.getValue().getRxSpeed();
			txSpeed = e.getValue().getTxSpeed();
			
			portSpeed = rxSpeed.add(txSpeed);
			logger.info("" + e.getKey().getNodeId() +" / " + e.getKey().getPortId() + " portSpeed: " + portSpeed.getBigInteger().toString());
			if(portSpeed.compareTo(threashold) > 0){
				abnormalTrafficSet.add(e.getKey());
			}
		}
	}
}
