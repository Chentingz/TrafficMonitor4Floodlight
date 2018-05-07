package net.floodlightcontroller.trafficmonitor;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPortStatsEntry;
import org.projectfloodlight.openflow.protocol.OFPortStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsRequest;
import org.projectfloodlight.openflow.protocol.OFStatsType;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFGroup;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;
import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import net.floodlightcontroller.trafficmonitor.web.TrafficMonitorWebRoutable;



public class TrafficMonitor implements IOFMessageListener, IFloodlightModule, ITrafficMonitorService {
	
	protected static final Logger logger = LoggerFactory.getLogger(TrafficMonitor.class);
	
	private IOFSwitchService 			switchService;
	private IThreadPoolService 			threadPoolService;		// 线程池
	private IRestApiService				restApiService;
	
	/* 配置参数 */
	private static long portStatsInterval = 10;			// 收集交换机端口统计量的周期,单位为秒
	private static U64  portSpeedThreshold = U64.ZERO;	// 端口流量阈值
	private static String action = null;				// 执行动作（针对异常流量）
	private static long actionDuration = 0;				// 动作持续时间
	private static U64 rateLimit = U64.ZERO;			// 执行限速动作时使用的速率限制
	
	private static Policy policy = new Policy();
	
	private static ScheduledFuture<?> portStatsCollector;		// 用于接收线程池的返回值，收集port_stats
	private static ScheduledFuture<?> flowStatsCollector;		// 用于接收线程池的返回值，收集flow_stats
	
	private static HashMap<NodePortTuple, SwitchPortStatistics> prePortStatsBuffer;	// 用于缓存先前的交换机端口统计信息，在init（）中进行初始化	
	private static HashMap<NodePortTuple, SwitchPortStatistics> portStatsBuffer;	// 用于缓存当前的交换机端口统计信息
	private static HashMap<NodePortTuple, SwitchPortStatistics> abnormalTraffic = new HashMap<NodePortTuple, SwitchPortStatistics>(); // 存储异常流量
	private static HashMap<NodePortTuple, Date> 				addFlowEntriesHistory = new HashMap<NodePortTuple, Date>();	// 流表项添加记录，防止下发重复流表项
	private static LinkedList<Event>							events = new LinkedList<Event>();								// 存储发生的事件
	private boolean isFirstTime2CollectSwitchStatistics = true;
	
	/**
	 * 线程，该类用于收集交换机端口统计信息并计算端口接收速率，发送速率
	 * 在startPortStatsCollection()中使用
	 */
	protected class PortStatsCollector implements Runnable{
		@Override
		public void run() {

			Map<DatapathId, List<OFStatsReply>> replies = getSwitchStatistics(switchService.getAllSwitchDpids(), OFStatsType.PORT);
			if(!replies.isEmpty()){
				logger.info("Got port_stats_replies");
				
				/* 第一次收集统计信息 */
				if(isFirstTime2CollectSwitchStatistics){
					isFirstTime2CollectSwitchStatistics = false;
				
					/* 记录收集的统计信息到prePortStatsReplies */
					savePortStatsReplies(prePortStatsBuffer, replies);
				}
				else{	/* 先前已经收集至少一次统计信息 */
					savePortStatsReplies(portStatsBuffer, replies);
					
					if(prePortStatsBuffer!=null)
					for(Entry<NodePortTuple, SwitchPortStatistics> entry : prePortStatsBuffer.entrySet()){
						NodePortTuple npt = entry.getKey();
						
						/* 计算端口接收速率和发送速率并更新端口统计信息 */
						if( portStatsBuffer.containsKey(npt)){
							U64 rxBytes = portStatsBuffer.get(npt).getRxBytes().subtract(prePortStatsBuffer.get(npt).getRxBytes());
							U64 txBytes = portStatsBuffer.get(npt).getTxBytes().subtract(prePortStatsBuffer.get(npt).getTxBytes());
							
							long period = portStatsBuffer.get(npt).getDuration() - prePortStatsBuffer.get(npt).getDuration();
							
							U64 rxSpeed = U64.ofRaw(rxBytes.getValue() / period);
							U64 txSpeed = U64.ofRaw(txBytes.getValue() / period);
											
							/* 更新 */
							portStatsBuffer.get(npt).setRxSpeed(rxSpeed);
							portStatsBuffer.get(npt).setTxSpeed(txSpeed);
						}					
					}
					
					/* 打印端口统计信息 
					logger.info("ready to print stats");
					for(Entry<NodePortTuple, SwitchPortStatistics> e : portStatsBuffer.entrySet()){
						e.getValue().printPortStatistics();
					}
					*/
					
					/* 更新prePortStatsBuffer */
					prePortStatsBuffer.clear();
					prePortStatsBuffer.putAll(portStatsBuffer);
					portStatsBuffer.clear();
					logger.info("prePortStatsBuffer updated");
					
					/* 端口速率分析 */
					abnormalTraffic.clear();
					TrafficAnalyzer.Analysis(prePortStatsBuffer, abnormalTraffic, policy);
					if(!abnormalTraffic.isEmpty()){
						TrafficControl.Control(switchService, abnormalTraffic, addFlowEntriesHistory, policy, events);
					}
					
				}
			}
		}
		
		/**
		 *  将收到的port_stats_reply保存到port_stats_buffer中
		 * @param portStatsBuffer 存储统计信息的缓冲区
		 * @param replies stats_reply消息列表
		 */
		private void savePortStatsReplies
			(HashMap<NodePortTuple, SwitchPortStatistics> portStatsBuffer, Map<DatapathId, List<OFStatsReply>> replies){
			for( Entry<DatapathId, List<OFStatsReply>> entry : replies.entrySet()){
				/* 解析port_stats_reply消息，将相关字段转存到SwitchPortStatistics类中, */
				OFPortStatsReply psr = (OFPortStatsReply)entry.getValue().get(0);
				for(OFPortStatsEntry e :  psr.getEntries()){
					NodePortTuple npt = new NodePortTuple(entry.getKey(), e.getPortNo());
					SwitchPortStatistics sps = new SwitchPortStatistics();
					
					sps.setDpid(npt.getNodeId());
					sps.setPortNo(npt.getPortId());
					sps.setRxBytes(e.getRxBytes());
					sps.setTxBytes(e.getTxBytes());
					sps.setDurationSec(e.getDurationSec());
					sps.setDurationNsec(e.getDurationNsec());
					
					sps.setRxPackets(e.getRxPackets());
					sps.setTxPackets(e.getTxPackets());
					sps.setRxDropped(e.getRxDropped());
					sps.setTxDropped(e.getTxDropped());
					
					/* 添加其他字段 */
					// ...
					
					/* 保存交换机id端口号的二元组和端口统计信息到缓冲区 */
					portStatsBuffer.put(npt, sps);
				}	
			}
		}
	
	}
	
	
	/**
	 * 线程，该类用于周期性的收集流表项统计信息并计算flow_speed(流带宽)
	 *
	 * 在startFlowStatsCollection()中使用
	 */
	protected class FlowStatsCollector implements Runnable{
		@Override
		public void run() {

			Map<DatapathId, List<OFStatsReply>> replies = getSwitchStatistics(switchService.getAllSwitchDpids(), OFStatsType.FLOW);
			// 计算flow_speed
		}
	}
	
	/**
	 * 	调用线程池服务，创建线程周期性执行PortStatsCollector类中的run()，portStatsInterval定义了执行周期
	 */
	private void startPortStatsCollection(){
		
		portStatsCollector = threadPoolService
		.getScheduledExecutor()
		.scheduleAtFixedRate(new PortStatsCollector(), portStatsInterval, portStatsInterval, TimeUnit.SECONDS);

		logger.warn("Port statistics collection thread(s) started");
	}
	
	/**
	 * 	调用线程池服务，创建线程周期性执行FlowStatsCollector类中的run()，portStatsInterval定义了执行周期
	 */
	private void startFlowStatsCollection(){
		
		flowStatsCollector = threadPoolService
		.getScheduledExecutor()
		.scheduleAtFixedRate(new FlowStatsCollector(), portStatsInterval, portStatsInterval, TimeUnit.SECONDS);
		
		logger.warn("Flow statistics collection thread(s) started");

	}
	
	
	/**
	 * 获取所有交换机的统计信息，通过创建GetStatsThread线程来完成stats_request的发送和stats_reply的接收，
	 * 即网络I/O操作由线程来完成
	 * @param allSwitchDpids
	 * @param statsType
	 * @return
	 */
	public Map<DatapathId, List<OFStatsReply>> getSwitchStatistics  
		(Set<DatapathId> dpids, OFStatsType statsType) {
		HashMap<DatapathId, List<OFStatsReply>> dpidRepliesMap = new HashMap<DatapathId, List<OFStatsReply>>();
		
		List<GetStatsThread> activeThreads = new ArrayList<GetStatsThread>(dpids.size());
		List<GetStatsThread> pendingRemovalThreads = new ArrayList<GetStatsThread>();
		GetStatsThread t;
		for (DatapathId d : dpids) {
			t = new GetStatsThread(d, statsType);
			activeThreads.add(t);
			t.start();
		}

		/* Join all the threads after the timeout. Set a hard timeout
		 * of 12 seconds for the threads to finish. If the thread has not
		 * finished the switch has not replied yet and therefore we won't
		 * add the switch's stats to the reply.
		 */
		for (int iSleepCycles = 0; iSleepCycles < portStatsInterval; iSleepCycles++) {
			/* 遍历activeThread，检测线程是否完成，若完成则记录dpid和replies，并将curThread加入到pendingRemovalThreads以待清除 */
			for (GetStatsThread curThread : activeThreads) {
				if (curThread.getState() == State.TERMINATED) {
					dpidRepliesMap.put(curThread.getSwitchId(), curThread.getStatsReplies());
					pendingRemovalThreads.add(curThread);
				}
			}

			/* remove the threads that have completed the queries to the switches */
			for (GetStatsThread curThread : pendingRemovalThreads) {
				activeThreads.remove(curThread);
			}
			
			/* clear the list so we don't try to double remove them */
			pendingRemovalThreads.clear();

			/* if we are done finish early */
			if (activeThreads.isEmpty()) {
				break;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error("Interrupted while waiting for statistics", e);
			}
		}

		return dpidRepliesMap;
	}
	
	/**
	 * 获取一台交换机的统计信息
	 * 发送stats_request并接收stats_reply
	 * @param dpid
	 * @param statsType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<OFStatsReply> getSwitchStatistics  
		(DatapathId dpid, OFStatsType statsType) {
		OFFactory my13Factory = OFFactories.getFactory(OFVersion.OF_13);
		OFStatsRequest<?> request = null;
		
		/* 根据statsType完成stats_request消息的封装 */
		switch(statsType){
		case PORT:
			request = my13Factory.buildPortStatsRequest()
								 .setPortNo(OFPort.ANY)
								 .build();
			break;
			
		case FLOW:
			Match match = my13Factory.buildMatch().build();
			request = my13Factory.buildFlowStatsRequest()
								 .setMatch(match)
								 .setOutPort(OFPort.ANY)
								 .setOutGroup(OFGroup.ANY)
								 .setTableId(TableId.ALL)
								 .build();	
			break;
			
		default:
			logger.error("OFStatsType unknown,unable to build stats request");
		}
		
		/* 向指定交换机发送stats_request，并接收stats_reply */
		IOFSwitch sw = switchService.getSwitch(dpid);
		ListenableFuture<?> future = null;
		
		if(sw != null && sw.isConnected() == true)
			future = sw.writeStatsRequest(request);
		List<OFStatsReply> repliesList = null;
		try {
			repliesList = (List<OFStatsReply>) future.get(portStatsInterval*1000 / 2, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			logger.error("Failure retrieving statistics from switch {}. {}", sw, e);
		}
		return repliesList;
	}
	
	/**
	 * 获取统计信息的线程类
	 * 对stats_msg的I/O由线程来完成
	 * 在run（）中调用getSwitchStatistics（）完成stats_request的发送和stats_reply的接收
	 *
	 */
	private class GetStatsThread extends Thread{
		private DatapathId dpid;
		private OFStatsType statsType;
		private	List<OFStatsReply> replies;
		
		public GetStatsThread(DatapathId dpid, OFStatsType statsType){
			this.dpid = dpid;
			this.statsType = statsType;
			this.replies = null;
		}
		
		public void run(){
			replies = getSwitchStatistics(dpid, statsType);
		}
		
		public DatapathId getSwitchId(){
			return dpid;
		}
		public List<OFStatsReply> getStatsReplies(){
			return replies;
		}
	}
	
	/*
	 * IFloodlightModule的实现
	 */
	
	/**
	 * 给OFMessage监听器加一个ID
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return TrafficMonitor.class.getSimpleName();
	}



	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		Collection<Class<? extends IFloodlightService>> l = new
				ArrayList<Class<? extends IFloodlightService>>();
				l.add(ITrafficMonitorService.class);
				return l;
	}
	
	/**
	 * tells the module system that we are the class that provides the service
	 * 告诉模块系统TrafficMonitor类提供ITrafficMonitorService服务
	 */
	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		Map<Class<? extends IFloodlightService>, IFloodlightService> m = new
				HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
				m.put(ITrafficMonitorService.class, this);
				return m;
	}

	
	/**
	 * 告知模块加载器在floodlight启动时将该模块加载。
	 */
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// TODO Auto-generated method stub
		   Collection<Class<? extends IFloodlightService>> l =
			        new ArrayList<Class<? extends IFloodlightService>>();
			    l.add(IOFSwitchService.class);
			    l.add(IThreadPoolService.class);
			    l.add(IRestApiService.class);
			    return l;
	}

	/**
	 * 将在控制器启动初期被调用，其主要功能是加载依赖关系并初始化数据结构。
	 */
	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		// TODO Auto-generated method stub
		switchService = context.getServiceImpl(IOFSwitchService.class);
		threadPoolService = context.getServiceImpl(IThreadPoolService.class);
		restApiService = context.getServiceImpl(IRestApiService.class);
		
		prePortStatsBuffer = new HashMap<NodePortTuple, SwitchPortStatistics>();
		portStatsBuffer = new HashMap<NodePortTuple, SwitchPortStatistics>();
		
	}


	/**
	 * 模块启动
	 */
	@Override
	public void startUp(FloodlightModuleContext context){
		/* 设置请求url */
		restApiService.addRestletRoutable(new TrafficMonitorWebRoutable());
		
		/* 收集交换机端口统计信息并计算port_speed */
		startPortStatsCollection();
		
		/* 收集流表项统计信息并计算flow_speed */
	//	startFlowStatsCollection();

	}
	
	/**
	 * 对 OF消息的处理（主要是PACKET_IN消息）
	 */
	@Override
	public net.floodlightcontroller.core.IListener.Command receive(
			IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/*
	 * ITrafficMonitorService 实现
	 */
	@Override
	public HashMap<NodePortTuple, SwitchPortStatistics> getPortStatistics() {
		// TODO Auto-generated method stub
		return prePortStatsBuffer;
	}	
	
	@Override
	public SwitchPortStatistics getPortStatistics(DatapathId dpid, OFPort port) {
		// TODO Auto-generated method stub
		return prePortStatsBuffer.get(new NodePortTuple(dpid, port));
	}


	@Override
	public Policy getPolicy() {
		// TODO Auto-generated method stub
		return policy;
	}

	@Override
	public void setPolicy(U64 portSpeedThreshold, String action,
			long actionDuration, U64 rateLimit) {
		policy.setPortSpeedThreshold(portSpeedThreshold);
		policy.setAction(action);
		policy.setActionDuration(actionDuration);
		policy.setRateLimit(rateLimit);
		
	}

	@Override
	public LinkedList<Event> getEvents() {
		// TODO Auto-generated method stub
		return events;
	}

}

