package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleDto {
  @NotNull(message="name is required")
  String name;
  
  @NotNull(message="Description is required")
  String description;
}
