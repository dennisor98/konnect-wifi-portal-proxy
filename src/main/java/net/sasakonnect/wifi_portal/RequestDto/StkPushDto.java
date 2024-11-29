package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class StkPushDto {
	@NotNull(message = "subscriptionPlanId is required")
	String subscriptionPlanId;	
	String phone;
}
