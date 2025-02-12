package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignRoleDto {
   @NotNull
   String userId;
   
   @NotNull
   String roleId;
}
