package net.floodlightcontroller.trafficmonitor.web;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.statistics.web.BandwidthResource;
import net.floodlightcontroller.statistics.web.SwitchStatisticsWebRoutable;
import net.floodlightcontroller.trafficmonitor.ITrafficMonitorService;
import net.floodlightcontroller.trafficmonitor.SwitchPortStatistics;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 这个类用于处理REST API请求，具体做法是获取url的字段，并用这些字段作为参数调用ITrafficMonitorService的方法
 * 如/wm/trafficmonitor/portstats/{dpid}/{port}/json
 * 取出dpid和port，并调用ITrafficMonitorService的getPortStatistics()，获取指定交换机端口的统计信息
 */
public class PortStatsResource extends ServerResource {
	private static final Logger logger = LoggerFactory.getLogger(BandwidthResource.class);
	
	@Get("json")
	public Object retrieve() {
		
		ITrafficMonitorService trafficMonitorService = (ITrafficMonitorService)getContext().getAttributes().get(ITrafficMonitorService.class.getCanonicalName());
		
		/* 从url路径中取出字符串dpid和port */
		String dpid_str = (String) getRequestAttributes().get(TrafficMonitorWebRoutable.DPID_STR);
	    String port_str = (String) getRequestAttributes().get(TrafficMonitorWebRoutable.PORT_STR);
		
	    
	    /* 根据字符串转换 */
		DatapathId dpid = DatapathId.of(dpid_str);
		OFPort 	   port = OFPort.of(Integer.parseInt(port_str));
		
	    
		/* 根据dpid和port通过ITrafficMonitorService获取端口统计信息 */
	    Set<SwitchPortStatistics> spsSet = new HashSet<SwitchPortStatistics>();
		if(dpid != null && port !=null){
			SwitchPortStatistics sps = trafficMonitorService.getPortStatistics(dpid, port);
			if(sps != null){
				spsSet.add(sps);
			}
			else
			{
				logger.info("port stats null");
			}
		}
		
		return spsSet;
	}
}
