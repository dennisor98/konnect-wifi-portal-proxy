package net.sasakonnect.wifi_portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.sasakonnect.wifi_portal.RequestDto.DelRoleDto;
import net.sasakonnect.wifi_portal.RequestDto.RoleDto;
import net.sasakonnect.wifi_portal.RequestDto.RoleEditDto;
import net.sasakonnect.wifi_portal.RequestDto.RolePermDto;
import net.sasakonnect.wifi_portal.annotations.CustomController;
import net.sasakonnect.wifi_portal.services.RoleService;

@CustomController
@RequestMapping("role")
@Tag(name="Role")
public class RolesController {
	@Autowired
	RoleService roleService;
	
  @GetMapping()
  public Object getRoles() {
	  return this.roleService.getAllRoles();
  }

  @PostMapping()
  public Object createRole(@Valid @RequestBody() RoleDto roleDto) {
	  return this.roleService.createRole(roleDto);
  }
  
  @DeleteMapping()
  public Object deleteRole(@Valid @RequestBody() DelRoleDto roleDto) {
	  return this.roleService.deleteRole(roleDto);
  }
  
  @PutMapping()
  public Object editRole(@Valid @RequestBody() RoleEditDto roleDto) {
	  return this.roleService.editRole(roleDto);
  }
  
  
  @PostMapping("/permissions")
  public Object assignPermissionsToRole(@Valid @RequestBody() RolePermDto roleDto) {
	  return this.roleService.assignPermissionsToRole(roleDto);
  }
  
  @GetMapping("/permissions")
  public Object getRolePermissions(@RequestParam(name="roleId",required=true) String roleId) {
	  return this.roleService.getRolePermissions(roleId);
  }
  
  @GetMapping("/allPermissions")
  public Object getAllPermissions() {
	  return this.roleService.getAllPermissions();
  }
}
