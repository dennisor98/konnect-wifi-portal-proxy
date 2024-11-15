package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PollMpesaDto {
  String MerchantRequestID;
  
  @NotNull(message="CheckoutRequestID is required")
  String   CheckoutRequestID;
  
  String   ResponseCode;
  
  String   ResponseDescription;
  
  String  CustomerMessage;
}
