package net.sasakonnect.wifi_portal.RequestDto.sdk;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MpesaResponse  implements Serializable{

    @JsonProperty("MerchantRequestID")
    private String MerchantRequestID;

    @JsonProperty("CheckoutRequestID")
    private String CheckoutRequestID;

    @JsonProperty("ResponseCode")
    private String ResponseCode;

    @JsonProperty("ResponseDescription")
    private String ResponseDescription;

    @JsonProperty("CustomerMessage")
    private String CustomerMessage;


  
}
