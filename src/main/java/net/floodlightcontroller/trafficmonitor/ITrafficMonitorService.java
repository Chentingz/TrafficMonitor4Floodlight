package net.floodlightcontroller.trafficmonitor;

import java.util.HashMap;
import java.util.LinkedList;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;

public interface ITrafficMonitorService extends IFloodlightService {

	// 获取所有交换机端口统计信息
	public HashMap<NodePortTuple, SwitchPortStatistics> getPortStatistics();
	
	// 获取指定交换机端口统计信息
	public SwitchPortStatistics getPortStatistics(DatapathId dpid, OFPort port);
	
	// 获取策略
	public Policy getPolicy();
	
	// 设置策略
	public void setPolicy(U64 portSpeedThreshold, String action, long actionDuration, U64 rateLimit);

	// 获取事件
	public LinkedList<Event> getEvents();

}
