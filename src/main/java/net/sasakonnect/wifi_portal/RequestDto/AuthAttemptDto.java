package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
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
