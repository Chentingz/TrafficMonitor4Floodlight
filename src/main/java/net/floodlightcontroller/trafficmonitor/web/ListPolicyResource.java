package net.floodlightcontroller.trafficmonitor.web;

import net.floodlightcontroller.trafficmonitor.ITrafficMonitorService;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * 列出策略配置的参数
 * 
 *
 */
public class ListPolicyResource extends ServerResource {
	@Get("json")
	public Object list(){
		ITrafficMonitorService trafficMonitorService = (ITrafficMonitorService) getContext().getAttributes().get(ITrafficMonitorService.class.getCanonicalName());
		return trafficMonitorService.getPolicy();
	}
}
