package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppToolKitPayDto {
	@NotNull()
	String mobileNumber;
	
	String staMac;
	
    Boolean actNow;
}
