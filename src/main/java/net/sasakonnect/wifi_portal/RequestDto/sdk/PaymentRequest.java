package net.sasakonnect.wifi_portal.RequestDto.sdk;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sasakonnect.wifi_portal.domain.App;
import net.sasakonnect.wifi_portal.domain.User;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest implements Serializable  {
	private static final long serialVersionUID = 1961213883468766701L;
	private long amount;
	private String phoneNumber;

	private String appKey;

	private String KonnectCheckoutID;

	private String ExternalCheckoutId;

	private App app;

	private User user;
	
	private String staMac;
}
