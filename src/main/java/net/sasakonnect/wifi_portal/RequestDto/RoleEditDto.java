package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleEditDto {
  @NotNull()
  String roleId;
  
  @NotNull()
  String name;
  
  @NotNull()
  String description;
  
  
}
