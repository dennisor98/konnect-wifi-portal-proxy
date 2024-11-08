package net.sasakonnect.wifi_portal.workers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.sasakonnect.wifi_portal.constants.GlobalPermissionsConstants;
import net.sasakonnect.wifi_portal.domain.Permission;
import net.sasakonnect.wifi_portal.domain.Role;
import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.domain.UserRole;
import net.sasakonnect.wifi_portal.repository.RolePermissionRepository;
import net.sasakonnect.wifi_portal.repository.RoleRepository;
import net.sasakonnect.wifi_portal.repository.UserRoleRepository;
import net.sasakonnect.wifi_portal.services.PermissionService;
import net.sasakonnect.wifi_portal.services.RoleService;
import net.sasakonnect.wifi_portal.services.UserService;

@Component
public class AppBootLoader implements ApplicationListener<ApplicationReadyEvent> {
	@Autowired
	RoleService roleService;
	@Autowired
	UserService userService;
	@Autowired
	RoleRepository roleRepository;
	@Autowired
	RolePermissionRepository rolePermissionRepository;
	@Autowired
	UserRoleRepository userRoleRepository;
	@Autowired
	PermissionService permissionService;
	
	@Override
	@Transactional
	public void onApplicationEvent(ApplicationReadyEvent event) {
		//create super user
		this.userService.createSuperUser("+254700000000");
      //create super admin role
		this.roleService.createSuperRole();
		
		var permissions = GlobalPermissionsConstants.scan();
		
		permissions.forEach((permmsion, desc) -> {
			var permission = Permission.builder().description(desc).name(permmsion).build();
			this.permissionService.insertPermissionIfNotExistsOrUpdateDescription(permission);
		});

		
		Optional<Role> superRoleOpt =  this.roleRepository.findByName("SUPER_ADMIN");
		if(superRoleOpt.isPresent()) {
			
			var role = superRoleOpt.get();
		Optional<User> userOpt =  this.userService.findUserByPhone("+254700000000");
		if(userOpt.isPresent()) {
			var user = userOpt.get();
			var userRole =  UserRole.builder().role(role).user(userOpt.get()).creator(user).build();
			Optional<UserRole> userRoleOpt =  this.userRoleRepository.findByUserAndRole(user,role);
			if(userRoleOpt.isEmpty()) {
				try {
					this.userRoleRepository.save(userRole);
				}catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}
			List<Permission> perms = this.permissionService.getAllPermissions();
			perms.forEach(p->{
				this.roleService.assignPermissionsNotAssignedToSuperRole(role, p);
			});
		}


	}
	
	

}
