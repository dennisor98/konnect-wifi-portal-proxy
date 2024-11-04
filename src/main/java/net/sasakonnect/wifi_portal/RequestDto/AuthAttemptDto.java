package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuthAttemptDto {
	@NotNull()
   String pageType;
   
	@NotNull()
   String vlan;
	
	@NotNull()
	String staMac;
	
	@NotNull()
	String staIp;
	
	@NotNull()
	String apMac;
	
	@NotNull()
	String apIp;
	
	@NotNull()
	String ssid;
	
	@NotNull()
	String acIp;
   
   
}
