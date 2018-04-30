package net.floodlightcontroller.trafficmonitor.web;

import java.io.IOException;
import java.util.Date;

import org.projectfloodlight.openflow.types.U64;

import net.floodlightcontroller.trafficmonitor.SwitchPortStatistics;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class SwitchPortStatisticsSerializer extends JsonSerializer<SwitchPortStatistics> {

	/**
	 *  对SwitchPortStatistics类对象进行序列化
	 *  打印指定的属性
	 */
	@Override
	public void serialize(SwitchPortStatistics sps, JsonGenerator jGen, SerializerProvider serializer)
			throws IOException, JsonProcessingException {
		jGen.configure(Feature.WRITE_NUMBERS_AS_STRINGS, true);

		{
			jGen.writeStartObject();
		
			jGen.writeFieldName("switch");
			{
				jGen.writeStartObject();
				jGen.writeStringField("dpid", sps.getDpid().toString());
				jGen.writeStringField("port no:", sps.getPortNo().toString());
				jGen.writeEndObject();
			}
			
			jGen.writeFieldName("port stats");
			{
				jGen.writeStartObject();
				jGen.writeStringField("rx speed(Bps)", sps.getRxSpeed().getBigInteger().toString());
				jGen.writeStringField("tx speed(Bps)", sps.getTxSpeed().getBigInteger().toString());
				jGen.writeStringField("update time", new Date(sps.getUpdateTime()).toString());
				
				jGen.writeStringField("rx bytes", sps.getRxBytes().getBigInteger().toString());
				jGen.writeStringField("tx bytes", sps.getTxBytes().getBigInteger().toString());
				
				jGen.writeStringField("rx packets", sps.getRxPackets().getBigInteger().toString());
				jGen.writeStringField("tx packets", sps.getTxPackets().getBigInteger().toString());
				
				jGen.writeStringField("rx dropped", sps.getRxDropped().getBigInteger().toString());
				jGen.writeStringField("tx dropped", sps.getTxDropped().getBigInteger().toString());
				jGen.writeEndObject();
			}
		
			jGen.writeEndObject();
		}
	}

}
