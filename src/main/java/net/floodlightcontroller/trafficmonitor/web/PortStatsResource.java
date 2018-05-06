package net.floodlightcontroller.trafficmonitor.web;

import java.util.Collections;
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
	private static final Logger logger = LoggerFactory.getLogger(PortStatsResource.class);
	
	@Get("json")
	public Object retrieve() {
		
		ITrafficMonitorService trafficMonitorService = (ITrafficMonitorService)getContext().getAttributes().get(ITrafficMonitorService.class.getCanonicalName());
		
		/* 从url路径中取出字符串dpid和port */
		String dpid_str = (String) getRequestAttributes().get(TrafficMonitorWebRoutable.DPID_STR);
	    String port_str = (String) getRequestAttributes().get(TrafficMonitorWebRoutable.PORT_STR);
		
	    
	    /* dpid和port初始化 */
		DatapathId dpid = DatapathId.NONE;
		OFPort 	   port = OFPort.ALL;
					
		
		/* 根据url字段里的dpid_str和port_str给dpid和port赋值 */
	    if (!dpid_str.trim().equalsIgnoreCase("all")) {
	            try {
	                dpid = DatapathId.of(dpid_str);
	            } catch (Exception e) {
	                logger.error("Could not parse DPID {}", dpid_str);
	                return Collections.singletonMap("ERROR", "Could not parse DPID " + dpid_str);
	            }
	    } /* else dpid为all */
	    
	    if (!port_str.trim().equalsIgnoreCase("all")) {
            try {
                port = OFPort.of(Integer.parseInt(port_str));
            } catch (Exception e) {
                logger.error("Could not parse port {}", port_str);
                return Collections.singletonMap("ERROR", "Could not parse port " + port_str);
            }
        } /* else port为all */
	    
		
		/* 根据dpid和port通过ITrafficMonitorService获取端口统计信息 */
	    Set<SwitchPortStatistics> spsSet = new HashSet<SwitchPortStatistics>();
	    
		if(!dpid.equals(DatapathId.NONE) && !port.equals(OFPort.ALL)){	/* 指定交换机指定端口 */
			SwitchPortStatistics sps = trafficMonitorService.getPortStatistics(dpid, port);
			if(sps != null){
				spsSet.add(sps);
			}
			else
			{
				logger.info("port stats null");
			}
		}
		else if(dpid.equals(DatapathId.NONE) && port.equals(OFPort.ALL)){	/* 获取全部交换机，全部端口的统计信息 */
			for(Entry<NodePortTuple, SwitchPortStatistics> e: trafficMonitorService.getPortStatistics().entrySet()){
				spsSet.add(e.getValue());
			}
		}
		
		return spsSet;
	}
}
