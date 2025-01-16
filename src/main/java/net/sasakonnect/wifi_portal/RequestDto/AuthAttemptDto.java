package net.sasakonnect.wifi_portal.RequestDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthAttemptDto {
   String pagetype;   
   String vlan;
	
	String staMac;
	
	String staIp;
	
	String apMac;
	
	String apIp;
	
	String ssid;
	
	String acIp;
   
   
}
