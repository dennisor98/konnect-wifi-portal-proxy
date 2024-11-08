package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PollMpesaDto {
  @NotNull(message="MerchantRequestID is required")
  String MerchantRequestID;
  
  @NotNull(message="CheckoutRequestID is required")
  String   CheckoutRequestID;
  
  @NotNull(message="ResponseCode is required")
  String   ResponseCode;
  
  @NotNull(message="ResponseDescription is required")
  String   ResponseDescription;
  
  String  CustomerMessage;
}
