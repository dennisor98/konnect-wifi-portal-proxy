package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConnectTvDto {
   @NotNull
   String staMac;
   
   @NotNull
   String publicIp;
   
   @NotNull
   String localIp;
   
   String interfaceMode;
   
   String pageType;
   
   
   
}
