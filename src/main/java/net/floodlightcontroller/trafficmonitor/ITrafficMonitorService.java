package net.floodlightcontroller.trafficmonitor;

import java.util.HashMap;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;

public interface ITrafficMonitorService extends IFloodlightService {

	// 获取所有交换机端口统计信息
	public HashMap<NodePortTuple, SwitchPortStatistics> getPortStatistics();
	
	// 获取指定交换机端口统计信息
	public SwitchPortStatistics getPortStatistics(DatapathId dpid, OFPort port);
	
}
