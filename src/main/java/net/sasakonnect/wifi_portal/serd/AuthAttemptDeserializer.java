package net.sasakonnect.wifi_portal.serd;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sasakonnect.wifi_portal.RequestDto.AuthAttemptDto;

public class AuthAttemptDeserializer extends JsonDeserializer<AuthAttemptDto> {
	@Override
	public AuthAttemptDto deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		String json = p.getText(); // Read the JSON string from the field
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(json, AuthAttemptDto.class);  // Deserialize into the AuthAttempt object
	}


}

