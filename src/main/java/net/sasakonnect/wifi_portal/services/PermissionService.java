package net.sasakonnect.wifi_portal.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sasakonnect.wifi_portal.domain.Permission;
import net.sasakonnect.wifi_portal.repository.PermissionsRepository;

@Service
public class PermissionService {
	@Autowired
	PermissionsRepository permissionsRepository;
	
   public void insertPermissionIfNotExistsOrUpdateDescription(Permission permission) {
	   Optional<Permission> permOpt = this.permissionsRepository.findByName(permission.getName());
	   
	   if(permOpt.isEmpty()) {
		   this.permissionsRepository.save(permission);
	   }else {
		   var perm = permOpt.get();
		   perm.setDescription(permission.getDescription());
		   this.permissionsRepository.save(perm);
	   }
   }
   
   public List<Permission> getAllPermissions() {
	   return this.permissionsRepository.findAll();
   }
}
