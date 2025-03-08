package net.sasakonnect.wifi_portal.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.sasakonnect.wifi_portal.domain.RolePermission;
import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.domain.UserRole;
import net.sasakonnect.wifi_portal.RequestDto.DelRoleDto;
import net.sasakonnect.wifi_portal.RequestDto.RoleDto;
import net.sasakonnect.wifi_portal.RequestDto.RoleEditDto;
import net.sasakonnect.wifi_portal.RequestDto.RolePermDto;
import net.sasakonnect.wifi_portal.domain.Permission;
import net.sasakonnect.wifi_portal.domain.Role;
import net.sasakonnect.wifi_portal.repository.PermissionsRepository;
import net.sasakonnect.wifi_portal.repository.RolePermissionRepository;
import net.sasakonnect.wifi_portal.repository.RoleRepository;
import net.sasakonnect.wifi_portal.repository.UserRepository;
import net.sasakonnect.wifi_portal.repository.UserRoleRepository;

@Service
public class RoleService {
  @Autowired
  RoleRepository roleRepository;
  @Autowired
  RolePermissionRepository rolePermissionRepository;
  @Autowired
  PermissionsRepository permissionRepository;
  @Autowired
  UserRepository userRepository;
  @Autowired
  UserRoleRepository userRoleRepository;
	
  public void createSuperRole() {
	  Optional<Role> roleOpt = this.roleRepository.findByName("SUPER_ADMIN");
	  if(roleOpt.isEmpty()) {
		  var role = Role.builder().name("SUPER_ADMIN").description("Has all permissions in the system and can perform any action").build();
		  this.roleRepository.save(role);
	  }
  }

  public void assignPermissionsNotAssignedToSuperRole(Role role,Permission permission) {
	  Optional<RolePermission> rolepermopt =  this.rolePermissionRepository.findByRoleAndPermission(role, permission);
	  if(rolepermopt.isEmpty()) {
		  var roleperm = RolePermission.builder().role(role).permission(permission).user(null).build();
		  try {
			  this.rolePermissionRepository.save(roleperm);
		  }catch(Exception ex) {
			  ex.printStackTrace();
		  }
	  }
  }
  
  
  public Object createRole(RoleDto roledto) {
	  User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	  Optional<Role> roleOpt =  this.roleRepository.findByName(roledto.getName());
	  if(roleOpt.isPresent()) {
		  Map<String,Object> map = new HashMap<>();
		  map.put("success", false);
		  map.put("message","Role already exists");

		  return ResponseEntity.status(HttpStatus.CONFLICT).body(map);
	  }

	  var role =  Role.builder().name(roledto.getName()).description(roledto.getDescription()).user(user).build();	
	  try {
		  this.roleRepository.save(role);
		  Map<String,Object> map = new HashMap<>();
		  map.put("success", false);
		  map.put("message","Role created");

		  return ResponseEntity.status(HttpStatus.OK).body(map);
	  }catch(Exception ex) {
		  ex.printStackTrace();
		  Map<String,Object> map = new HashMap<>();
		  map.put("success", false);
		  map.put("message","A server error occured");

		  return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(map);

	  }
  }
  
  public Object getAllRoles() {
	  List<Role> roleList = this.roleRepository.findAll();
	  var roles =  roleList.stream()
			  .map(r->{
				  Map<String,Object> role =  new HashMap<>();
				  role.put("id",r.getId());
				  role.put("name",r.getName());
				  role.put("description",r.getDescription());
				  role.put("createdAt",r.getCreatedAt());
				  role.put("updatedAt",r.getUpdatedAt());
				  role.put("createdBy",r.getUser() !=null ? r.getUser().getFirstname()+"  "+r.getUser().getLastname() : null);

				  return role;
			  }).collect(Collectors.toList());

	  Map<String,Object> map = new HashMap<>();
	  map.put("saddRoleuccess",true);
	  map.put("message", "Request complete");
	  map.put("roles",roles);
	  return ResponseEntity.status(HttpStatus.OK).body(map);
  }
  
  @Transactional
  public Object assignPermissionsToRole(RolePermDto dto) {
	  User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	  Optional<Role> roleOpt = this.roleRepository.findById(dto.getRoleId());
	  if(roleOpt.isEmpty()) {
		  Map<String,Object> map = new HashMap<>();
		  map.put("success", false);
		  map.put("message","Invalid roleId");

		  return ResponseEntity.status(HttpStatus.BAD_REQUEST);
	  }
	  var role = roleOpt.get();
	  //prevent editing super_admin role_permissions
	  if(role.getName().equalsIgnoreCase("SUPER_ADMIN")) {
		  Map<String,Object> map = new HashMap<>();
		  map.put("success",false);
		  map.put("message","Operation not permitted");

		  return ResponseEntity.status(HttpStatus.FORBIDDEN).body(map);
	  }
	  List<RolePermission> rolepermOpt =  this.rolePermissionRepository.findByRole(role);

	  if(!rolepermOpt.isEmpty()) {
		  this.rolePermissionRepository.deleteAll(rolepermOpt);
	  }
	  var permIds = dto.getPermissionIds();
	  List<RolePermission> permissions =  new ArrayList<>();
	  if(permIds.size() > 0) {
		  permIds.forEach(id->{
			  Optional<Permission> permOpt = this.permissionRepository.findById(id);
			  if(permOpt.isPresent()) {
				  var perm = permOpt.get();
				  var  roleperm = RolePermission.builder().role(role).permission(perm).user(user).build();
				  permissions.add(roleperm);
			  }
		  });
		  try {
			  this.rolePermissionRepository.saveAll(permissions);
			  Map<String,Object> map = new HashMap<>();
			  map.put("success", true);
			  map.put("message","Permissions assigned to role");

			  return ResponseEntity.status(HttpStatus.OK).body(map);
		  }catch(Exception ex) {
			  ex.printStackTrace();
			  Map<String,Object> map = new HashMap<>();
			  map.put("success", false);
			  map.put("message","A server error occured");

			  return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(map);
		  }


	  }
	  return null;
  }
  
  
  @Transactional
  public Object deleteRole(DelRoleDto dto) {
	  Optional<Role> roleOpt =  this.roleRepository.findById(dto.getRoleId());
	  if(roleOpt.isPresent()) {
		  var role = roleOpt.get();
		  if(role.getName().equalsIgnoreCase("SUPER_ADMIN")) {
			  Map<String,Object> map = new HashMap<>();
			  map.put("success",false);
			  map.put("message","Operation not permitted");

			  return ResponseEntity.status(HttpStatus.FORBIDDEN).body(map);
		  }
		  List<RolePermission> rolepermOpt =  this.rolePermissionRepository.findByRole(role);

		  if(!rolepermOpt.isEmpty()) {
			  this.rolePermissionRepository.deleteAll(rolepermOpt);
		  }

		  try {
			  this.roleRepository.delete(role);
			  Map<String,Object> map = new HashMap<>();
			  map.put("success",true);
			  map.put("message","Role deleted");
			  return ResponseEntity.status(HttpStatus.OK).body(map);
		  }catch(Exception ex) {
			  Map<String,Object> map = new HashMap<>();
			  map.put("success",false);
			  map.put("message","A server error encountered");

			  return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(map);
		  }


	  }else {
		  Map<String,Object> map = new HashMap<>();
		  map.put("success",false);
		  map.put("message","Invalid roleId");

		  return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
	  }
  }
  
  public Object editRole(RoleEditDto roledto) {
	  Optional<Role> roleOpt = this.roleRepository.findById(roledto.getRoleId());
	  if(roleOpt.isPresent()) {
		  var role = roleOpt.get();
		  if(role.getName().equalsIgnoreCase("SUPER_ADMIN")) {
			  Map<String,Object> map = new HashMap<>();
			  map.put("success",false);
			  map.put("message","Operation not permitted");

			  return ResponseEntity.status(HttpStatus.FORBIDDEN).body(map);
		  }

		  role.setName(roledto.getName());
		  role.setDescription(roledto.getDescription());

		  try {
			  Map<String,Object> map = new HashMap<>();
			  map.put("success",true);
			  map.put("message","Role edited");
			  this.roleRepository.save(role);
			  return ResponseEntity.status(HttpStatus.OK).body(map);
		  }catch(Exception ex) {
			  ex.printStackTrace();
			  Map<String,Object> map = new HashMap<>();
			  map.put("success",false);
			  map.put("message","A server error encountered");

			  return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(map);
		  }

	  }else {
		  Map<String,Object> map = new HashMap<>();
		  map.put("success",false);
		  map.put("message","Invalid roleId");

		  return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
	  }
  }
  
  
  public Object getRolePermissions(String roleId) {
	  Optional<Role> roleOpt = this.roleRepository.findById(roleId);
	  if(roleOpt.isEmpty()) {
		  Map<String,Object> map = new HashMap<>();
		  map.put("success",false);
		  map.put("message","Invalid roleId");

		  return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
	  }
	  var role = roleOpt.get();
	  List<Permission> roleperm = this.rolePermissionRepository.findPermissionsByRole(role);
	  
	  var permissions =  roleperm.stream()
			             .map(p->{
			            	 Map<String,Object> map = new HashMap<>();
			            	 map.put("id",p.getId());
			            	 map.put("name",p.getName());
			            	 map.put("description",p.getDescription());
			            	 
			            	 return map;
			             }).collect(Collectors.toList());
		  Map<String,Object> map = new HashMap<>();
		  map.put("success",true);
		  map.put("message","Request complete");
		  map.put("permissions",permissions);
	  
		  return ResponseEntity.status(HttpStatus.OK).body(map);
  }
  
  
 public Object getAllPermissions() {
	 List<Permission> permissionList = this.permissionRepository.findAll();
	 
	var permissions = permissionList.stream()
	               .map(p->{
	            	   Map<String,Object> map = new HashMap<>();
	            	   map.put("id", p.getId());
	            	   map.put("name", p.getName());
	            	   map.put("description", p.getDescription());
	            	   return map;
	               }).collect(Collectors.toList());
	 Map<String,Object> map = new HashMap<>();
	  map.put("success",true);
	  map.put("message","Request complete");
	  map.put("permissions",permissions);
	return ResponseEntity.status(HttpStatus.OK).body(map);
 }
 
 public Object getUserRoleByUserId(String userId) {
	 Optional<User> userOpt = this.userRepository.findById(userId);
	 if(userOpt.isEmpty()) {
		 ObjectNode resp = JsonNodeFactory.instance.objectNode();
		 resp.put("success",false);
		 resp.put("message","Invalid userId");
		 
		 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
		 
	 }
	 var user = userOpt.get();
	 Role role = null;
	 Optional<Role> roleOpt = this.userRoleRepository.findRoleByUser(user);
	 if(roleOpt.isPresent()) {
		 role = roleOpt.get();
	 }
	 
	 Map<String,Object> res =  new HashMap<>();
	 res.put("success",true);
	 res.put("message","Request complete");
	 res.put("role",role);
	 return ResponseEntity.status(HttpStatus.OK).body(res);
 }
 
 public Object getRoleUsers(String roleId) {
	 Optional<Role> roleOpt = this.roleRepository.findById(roleId);
	 if(roleOpt.isEmpty()) {
		 Map<String,Object> res = new HashMap<>();
		 res.put("success",false);
		 res.put("message","Invalid roleId");

		 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
	 }
	 
	 Role role = roleOpt.get();
	 
	 List<User> roleUsersList = this.userRoleRepository.findRoleUsers(role);
	 var users = roleUsersList.stream()
			 .map(u->{
				 Map<String,Object> map = new HashMap<>();
				 map.put("id",u.getId());
				 map.put("name",u.getFirstname()+" "+u.getLastname());
				 map.put("phone",u.getPhone());
				 
				 return map;
			 }).collect(Collectors.toList());
	 
	 Map<String,Object> response = new HashMap<>();
	 response.put("success",true);
	 response.put("message","Request complete");
	 response.put("users",users);
	 
	 return ResponseEntity.status(HttpStatus.OK).body(response);
	 
 }
 
 public Object deleteUserRole(String userId) {
	 User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	 if(userId.equalsIgnoreCase(loggedInUser.getId())) {
		 Map<String,Object> res = new HashMap<>();
		 res.put("success",false);
		 res.put("message","Invalid operation");
		 
		 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res); 
	 }
	 Optional<User> userOpt = this.userRepository.findById(userId);
	 if(userOpt.isEmpty()) {
		 Map<String,Object> res = new HashMap<>();
		 res.put("success",false);
		 res.put("message","Invalid userId");
		 
		 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
	 }
	 
	 User user = userOpt.get();
	 Optional<UserRole> userRoleOpt = this.userRoleRepository.findByUser(user);
	 if(userRoleOpt.isEmpty()) {
		 Map<String,Object> res = new HashMap<>();
		 
		 res.put("success",false);
		 res.put("message","Invalid operation");
		 
		 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
		 
	 }
	 
	 UserRole userRole = userRoleOpt.get();
	 try {
		 this.userRoleRepository.delete(userRole);
		 Map<String,Object> res = new HashMap<>();
		 res.put("success", true);
		 res.put("message","User removed from role!");
		 
		 return ResponseEntity.status(HttpStatus.OK).body(res);
	 }catch(Exception ex) {
		 ex.printStackTrace();
		 Map<String,Object> res = new HashMap<>();
		 res.put("success",false);
		 res.put("message","Error occured while processing request");
		 
		 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
	 }
	 
 }
 
 
}
