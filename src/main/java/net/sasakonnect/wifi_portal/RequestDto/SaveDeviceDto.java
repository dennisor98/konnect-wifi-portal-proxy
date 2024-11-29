package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveDeviceDto {
   @NotNull(message="name is required")
   String name;
   
   @NotNull(message="staMac is required")
   String staMac;
   
}
