package net.floodlightcontroller.trafficmonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.python.antlr.PythonParser.return_stmt_return;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.linkdiscovery.Link;



public class TrafficControl {
	private static final Logger logger = LoggerFactory.getLogger(TrafficControl.class);
	private static final String URL_ADD_DELETE_FLOW = "http://localhost:8080/wm/staticentrypusher/json";
	private static int countFlow = 0;
	
	public static void Control(IOFSwitchService switchService, HashMap<NodePortTuple, SwitchPortStatistics> abnormalTraffic, HashMap<NodePortTuple, Date> addFlowEntryHistory, Policy policy, LinkedList<Event> events){		
		for(Entry<NodePortTuple, SwitchPortStatistics> e : abnormalTraffic.entrySet()){
			NodePortTuple npt = e.getKey();
			SwitchPortStatistics sps = e.getValue();
			IOFSwitch sw = switchService.getSwitch(npt.getNodeId());
				
			if(!addFlowEntryHistory.containsKey(npt)){	/* 没有下发过流表项 */
				addFlowEntryHistory.put(npt,new Date());
				
				switch(policy.getAction()){
				case Policy.ACTION_NONE:
					break;
				case Policy.ACTION_DROP:
					int hardTimeout = (int) policy.getActionDuration();
					dropPacket(sw, npt.getPortId().getPortNumber(), hardTimeout, countFlow++);
					events.add(new Event(sps, policy));
					break;
				case Policy.ACTION_LIMIT:
					break;
				}

				
			}
			else{	 /* 已经下发过流表项，检测该流表项是否过期 */				
				switch(policy.getAction()){
				case Policy.ACTION_DROP:
					Date currentTime = new Date();
					long period = (currentTime.getTime() - addFlowEntryHistory.get(npt).getTime()) / 1000;	// 换算成second
					if(period > policy.getActionDuration()){
						logger.info("flow {match:" + npt.getNodeId() + " / " + npt.getPortId() + ", action:drop} expired!");
						addFlowEntryHistory.remove(npt);
					}
				}
				
			}
		}
		countFlow = 0;
	}
	
	public static void dropPacket(IOFSwitch sw, int inPortNumber, int hardTimeout, int countFLow){
		//解析传进来的属性
		HashMap<String, String> flow1 = new HashMap<String, String>();
		flow1.put("switch", sw.getId().toString());
		flow1.put("name", "flow" + countFLow);
		flow1.put("in_port", String.valueOf(inPortNumber));
		flow1.put("cookie", "0");
		flow1.put("priority", "32768");
		flow1.put("active", "true");
		flow1.put("hard_timeout", String.valueOf(hardTimeout));
		String r1 = addFlow(sw.getId().toString(), flow1);
		logger.info(r1);
	}
	
	public static String addFlow(String did,HashMap<String,String> flow)
	{
		String result = sendPost(URL_ADD_DELETE_FLOW,hashMapToJson(flow));
		return result;
	}
	
	public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }
	
	public static String hashMapToJson(HashMap map) {  
        String string = "{";  
        for (Iterator it = map.entrySet().iterator(); it.hasNext();) {  
            Entry e = (Entry) it.next();  
            string += "\"" + e.getKey() + "\":";  
            string += "\"" + e.getValue() + "\",";  
        }  
        string = string.substring(0, string.lastIndexOf(","));  
        string += "}";  
        logger.info(string);
        return string;
    } 
	
	
	
	
	
}
