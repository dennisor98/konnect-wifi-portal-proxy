package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAppDto {
	@NotNull
	String appId;

	@NotNull
	String appName;

	@NotNull
	String appKey;

	@NotNull
	String appSecret;

	@NotNull
	String callBackUrl;

	String businessShortCode;

	Boolean isActive;

	String consumerKey;

	String consumerSecret;

	String mpesaTillNo;
}
