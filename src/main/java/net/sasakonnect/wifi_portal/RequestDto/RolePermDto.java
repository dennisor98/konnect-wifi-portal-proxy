package net.sasakonnect.wifi_portal.RequestDto;

import java.util.List;

import lombok.Data;

@Data
public class RolePermDto {
  List<String> permissionIds;
  String roleId;
}
