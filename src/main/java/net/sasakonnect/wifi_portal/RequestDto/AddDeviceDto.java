package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddDeviceDto {

  @NotNull()
  AuthAttemptDto authAttempt;
  
  @NotNull()
  String id;
  
  @NotNull()
  String code;
}
