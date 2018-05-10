package net.floodlightcontroller.trafficmonitor.web;

import java.io.IOException;

import net.floodlightcontroller.trafficmonitor.Policy;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class PolicySerializer extends JsonSerializer<Policy> {

	@Override
	public void serialize(Policy p, JsonGenerator jGen,
			SerializerProvider serializer) throws IOException,
			JsonProcessingException {
		
		jGen.writeStartObject();
		jGen.writeStringField("traffic_threshold", p.getTrafficThreshold().getBigInteger().toString());
		jGen.writeStringField("action", p.getAction());
		jGen.writeStringField("action_duration", String.valueOf(p.getActionDuration()));
		jGen.writeStringField("rate_limit", p.getRateLimit().getBigInteger().toString());
		jGen.writeEndObject();
		
	}

}
