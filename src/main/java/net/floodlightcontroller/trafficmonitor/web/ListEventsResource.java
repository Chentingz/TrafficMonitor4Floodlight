package net.floodlightcontroller.trafficmonitor.web;

import net.floodlightcontroller.trafficmonitor.ITrafficMonitorService;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class ListEventsResource extends ServerResource {
	@Get("json")
	public Object list(){
		ITrafficMonitorService trafficMonitorService = (ITrafficMonitorService) getContext().getAttributes().get(ITrafficMonitorService.class.getCanonicalName());	
		return trafficMonitorService.getEvents();
	}
}
