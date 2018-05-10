package net.floodlightcontroller.trafficmonitor.web;

import java.io.IOException;
import java.text.DateFormat;

import net.floodlightcontroller.trafficmonitor.Event;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class EventSerializer extends JsonSerializer<Event> {

	@Override
	public void serialize(Event event, JsonGenerator jGen, SerializerProvider serializer)
			throws IOException, JsonProcessingException {
		jGen.writeStartObject();
		jGen.writeStringField("time", DateFormat.getDateTimeInstance().format(event.getTime()));
		jGen.writeFieldName("source");
			jGen.writeStartObject();
			jGen.writeStringField("dpid", event.getSource().getNodeId().toString());
			jGen.writeStringField("port_no", event.getSource().getPortId().toString());
			jGen.writeEndObject();
		jGen.writeFieldName("description");	
			jGen.writeStartObject();
			jGen.writeStringField("traffic_threshold", event.getPolicy().getTrafficThreshold().getBigInteger().toString());
			jGen.writeStringField("rx_speed", event.getRxSpeed().getBigInteger().toString());
			jGen.writeStringField("tx_speed", event.getTxSpeed().getBigInteger().toString());
			jGen.writeEndObject();
		jGen.writeFieldName("system_action");
			jGen.writeStartObject();
			jGen.writeStringField("action", event.getPolicy().getAction());
			jGen.writeStringField("action_duration", String.valueOf(event.getPolicy().getActionDuration()));
			jGen.writeStringField("rate_limit", event.getPolicy().getRateLimit().getBigInteger().toString());
			jGen.writeEndObject();
		jGen.writeEndObject();
		
	}

}
