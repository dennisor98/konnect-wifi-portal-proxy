package net.sasakonnect.wifi_portal.RequestDto.sdk;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PaymentRequest implements Serializable  {
	private static final long serialVersionUID = 1961213883468766701L;
	long amount;
	String phoneNumber;
	@ApiModelProperty(hidden = true)
    private String appKey;
	@ApiModelProperty(hidden = true)
    private String KonnectCheckoutID;
	@ApiModelProperty(hidden = true)
    private String ExternalCheckoutId;
}
