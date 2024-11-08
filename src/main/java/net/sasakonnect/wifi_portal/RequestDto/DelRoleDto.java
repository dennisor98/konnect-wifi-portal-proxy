package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DelRoleDto {
  @NotNull(message="roleId is required")
  String roleId;
}
