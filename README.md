# TrafficMonitor4Floodlight
添加了流量监控模块的Floodlight控制器，主要实现交换机端口收发流量可视化，异常流量检测与控制。

## Log：
* **2018/5/29**  
更新下发流表的条件，**只有当检测端口出现异常流量且该链路的另一端为终端时，才下发流表。** 实现定位异常流量源，并从异常源头进行安全处理。  

* **2018/5/9**  
查阅meter有关资料，将mininet中的Open vSwitch更新到 **2.9.0**。  
`（目前Open vSwitch 2.8.0 以上版本才支持meter，且只在userspace datapath上实现，kernel datapath上未实现，也就是说添加meter只能在Open vSwitch上通过命令实现，无法通过控制器发meter_modification_message来实现。）`  
[Does Open vSwitch support OpenFlow meters?](http://docs.openvswitch.org/en/latest/faq/qos/)  
[OVS常用命令与使用总结](https://blog.csdn.net/rocson001/article/details/73163041)  
[码农学ODL之流量限速实战](https://www.sdnlab.com/17972.html)  
[OpenFlow Meter的坑](https://www.cnblogs.com/wpqwpq/p/7832650.html)  

* **2018/5/7**  
添加事件展示功能  

![TrafficMonitor-Events.png](https://github.com/Chentingz/TrafficMonitor4Floodlight/blob/master/img4ReadMe/TrafficMonitor-Events.png)  

* **2018/5/6**  
添加策略配置功能，可通过REST API配置、获取策略。添加流量分析和控制功能，实现端口流量超过阈值，根据配置策略实施异常流量控制 **（目前只实现流量丢弃，限速未实现）**  

![TrafficMonitor-SwitchConnected.png](https://github.com/Chentingz/TrafficMonitor4Floodlight/blob/master/img4ReadMe/TrafficMonitor-SwitchConnected.png)  

![TrafficMonitor-Policy.png](https://github.com/Chentingz/TrafficMonitor4Floodlight/blob/master/img4ReadMe/TrafficMonitor-Policy.png)  

![TrafficMonitor-PolicyConfig.png](https://github.com/Chentingz/TrafficMonitor4Floodlight/blob/master/img4ReadMe/TrafficMonitor-PolicyConfig.png)  

![PortStatsDetail-LiveTraffic.png](https://github.com/Chentingz/TrafficMonitor4Floodlight/blob/master/img4ReadMe/PortStatsDetail-LiveTraffic.png)  

![PortStatsDetail-Statistics.png](https://github.com/Chentingz/TrafficMonitor4Floodlight/blob/master/img4ReadMe/PortStatsDetail-Statistics.png)  

* **2018/5/4**  
完善trafficMonitor.html和portStatsDetail.html，实现trafficMonitor.html跳转到portStatsDetail.html，并且在portStatsDetail.html中能够实时显示交换机端口流量  

![Areaspline.png](https://github.com/Chentingz/TrafficMonitor4Floodlight/blob/master/img4ReadMe/Areaspline%20.png)  

* **2018/5/3**  
添加portStatsDetail.html页面，用 **highchart（js图表库）** 以曲线图形式实现交换机端口流量实时显示  

![portStatsDetail-html.png](https://github.com/Chentingz/TrafficMonitor4Floodlight/blob/master/img4ReadMe/portStatsDetail-html.png)  

* **2018/4/30**  
添加用于查询交换机端口统计信息的REST API,输入`http://localhost:8080/wm/trafficmonitor/portstats/<dpid>/<portno>/json`即可获取指定交换机端口的统计信息  

![TrafficMonitor API - Port Stats.png](https://github.com/Chentingz/TrafficMonitor4Floodlight/blob/master/img4ReadMe/TrafficMonitor%20API%20-%20Port%20Stats.png)
  
## ToDoList:
- [x] TrafficMonitor.html与 portStatsDetail.html之间的参数传递（dpid + port），实现通过点击TrafficMonitor.html表格中的某行数据，跳转到portStatsDetail.html，打印指定交换机端口的实时信息（portStatsDetail.html?dpid=<dpid>&port=<port>)  
  
- [x] portStatsDetail.html中的曲线图x轴时间改为具体时间，如（23:51:23），数据提示框的时间格式改为（2018-5-2 23:51:23），延长曲线图数据刷新时间  

- [x] portStatsDetail.html添加表格显示rx_bytes，tx_bytes，rx_packets，tx_packets等统计信息

- [x] TrafficMonitor模块添加流量分析部分，监控进出端口流量大小，超出阈值，下发流表丢弃

- [x] traffcMonitor.html添加事件表格，打印异常流量产生的位置（dpid + port）及时间 

- [x] 使用openflow协议的meter表，实现端口某个方向的限速
  
