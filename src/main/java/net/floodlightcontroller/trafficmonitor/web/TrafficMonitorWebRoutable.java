package net.floodlightcontroller.trafficmonitor.web;

import java.util.HashSet;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import net.floodlightcontroller.restserver.RestletRoutable;

public class TrafficMonitorWebRoutable implements RestletRoutable {
	protected static final String DPID_STR = "dpid";
	protected static final String PORT_STR = "port";
	protected static final String ACTION_DROP_STR = "drop";
	protected static final String ACTION_RATE_LIMIT_STR = "rate-limit";
		
	@Override
	public Restlet getRestlet(Context context) {
		Router router = new Router(context);
		/* 将url路径与处理方法绑定 */
		router.attach("/portstats/{" + DPID_STR + "}/{" + PORT_STR + "}/json", PortStatsResource.class);
		router.attach("/policyconf/json", PolicyConfigResource.class);
		router.attach("/policyconf/list/json", ListPolicyConfigResource.class);
		return router;
	}

	@Override
	public String basePath() {
		return "/wm/trafficmonitor";
	}

}
