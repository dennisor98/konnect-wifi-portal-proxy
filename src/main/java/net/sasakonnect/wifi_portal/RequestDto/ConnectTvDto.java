package net.sasakonnect.wifi_portal.RequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConnectTvDto {
   String staMac;
   
   String publicIp;
   
   
   String staIp;
   
   String vlan;
   
   String mode;
   
   
}
