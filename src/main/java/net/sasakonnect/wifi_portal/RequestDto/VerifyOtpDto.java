package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class VerifyOtpDto {
 @NotNull(message="phone is required")
  String phone;
 
 @NotNull(message="code is required")
 String code;
}
