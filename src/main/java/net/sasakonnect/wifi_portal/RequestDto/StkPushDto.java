package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class StkPushDto {
	@NotNull(message = "firstname is required")
	String firstname;
	
	@NotNull(message = "phone is required")
	String phone;
	
	@NotNull(message = "subscriptionPlanId is required")
	String subscriptionPlanId;
	
	@NotNull(message = "amount is required")
	String amount;
	
	@NotNull(message = "amount is required")
	String ipAddress;
	
	@NotNull(message = "authAttempt is required")
	String authAttempt;
	
	@NotNull(message = "userId is required")
	String userId;
	
	String smsContent;
}
