package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ToolkitPayDto {
   @NotNull()
   String mobileNumber;
   
   String appKey;
   
   String userId;
   
}
