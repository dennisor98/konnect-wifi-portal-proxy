package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClientSubDto {
  @NotNull(message="phone is required")
  String phone;
}
