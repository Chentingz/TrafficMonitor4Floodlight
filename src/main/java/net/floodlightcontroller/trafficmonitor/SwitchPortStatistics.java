package net.floodlightcontroller.trafficmonitor;

import java.util.Date;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.floodlightcontroller.trafficmonitor.web.SwitchPortStatisticsSerializer;

@JsonSerialize(using=SwitchPortStatisticsSerializer.class)
public class SwitchPortStatistics {
	private DatapathId 	dpid;
	private OFPort		portNo;
	
	private U64			rxSpeed;
	private U64			txSpeed;
	private U64 		linkBandwidth; 
	
	private Date		updateTime;
	
	private U64			rxBytes;
	private U64			txBytes;
	private long		durationSec;
	private long		durationNsec;
	
	private U64			rxPackets;
	private U64			txPackets;
	private U64			rxDropped;
	private U64			txDropped;
	
	public SwitchPortStatistics(){ 
		dpid = DatapathId.NONE;
		portNo = null;
		rxSpeed = txSpeed = linkBandwidth  = U64.ZERO;
		updateTime = new Date();
		rxBytes = txBytes = U64.ZERO;
		durationSec = durationNsec = 0;
		rxPackets = txPackets = rxDropped = txDropped = U64.ZERO;
	}
	
	public void setDpid(DatapathId dpid) {
		this.dpid = dpid;
	}

	public void setPortNo(OFPort portNo) {
		this.portNo = portNo;
	}

	public void setRxSpeed(U64 rxSpeed) {
		this.rxSpeed = rxSpeed;
	}

	public void setTxSpeed(U64 txSpeed) {
		this.txSpeed = txSpeed;
	}

	public void setLinkBandwidth(U64 linkBandwidth) {
		this.linkBandwidth = linkBandwidth;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public void setRxPackets(U64 rxPackets) {
		this.rxPackets = rxPackets;
	}

	public void setTxPackets(U64 txPackets) {
		this.txPackets = txPackets;
	}

	public void setRxBytes(U64 rxBytes) {
		this.rxBytes = rxBytes;
	}

	public void setTxBytes(U64 txBytes) {
		this.txBytes = txBytes;
	}

	public void setRxDropped(U64 rxDropped) {
		this.rxDropped = rxDropped;
	}

	public void setTxDropped(U64 txDropped) {
		this.txDropped = txDropped;
	}

	public void setDurationSec(long durationSec) {
		this.durationSec = durationSec;
	}

	public void setDurationNsec(long durationNsec) {
		this.durationNsec = durationNsec;
	}
	public DatapathId getDpid() {
		return dpid;
	}

	public OFPort getPortNo() {
		return portNo;
	}

	public U64 getRxSpeed() {
		return rxSpeed;
	}

	public U64 getTxSpeed() {
		return txSpeed;
	}

	public U64 getRxPackets() {
		return rxPackets;
	}

	public U64 getTxPackets() {
		return txPackets;
	}

	public U64 getRxBytes() {
		return rxBytes;
	}

	public U64 getTxBytes() {
		return txBytes;
	}

	public U64 getRxDropped() {
		return rxDropped;
	}

	public U64 getTxDropped() {
		return txDropped;
	}

	public long getDurationSec() {
		return durationSec;
	}

	public long getDurationNsec() {
		return durationNsec;
	}
	
	public U64 getLinkBandwidth(){
		return linkBandwidth;
	}
	public long getUpdateTime(){
		return updateTime.getTime();
	}
	
	public long getDuration(){
		return (long) (durationSec + durationNsec / Math.pow(10, 9));
	}
	
	public void printPortStatistics(){
		String stats = "";
		
		stats += "switch dpid: " + dpid + "\n";
		stats += "port no: " + portNo + "\n";
		stats += "rx speed: " + rxSpeed.getBigInteger() + " Bps\n";
		stats += "tx speed: " + txSpeed.getBigInteger()  + " Bps\n";
		stats += "update time: " + updateTime + "\n";
		
		System.out.println(stats);		
	}

}
