package net.sasakonnect.wifi_portal.aspect;

import java.util.Optional;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import net.sasakonnect.wifi_portal.annotations.HasPermission;
import net.sasakonnect.wifi_portal.domain.Permission;
import net.sasakonnect.wifi_portal.domain.Role;
import net.sasakonnect.wifi_portal.domain.RolePermission;
import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.repository.PermissionsRepository;
import net.sasakonnect.wifi_portal.repository.RolePermissionRepository;
import net.sasakonnect.wifi_portal.repository.UserRoleRepository;

@Aspect
@Component
public class PermissionEvaluatorAspect {
	
	@Autowired
	UserRoleRepository  userRoleRepository;
	@Autowired
	RolePermissionRepository rolePermissionRepository;
	@Autowired
	PermissionsRepository permissionRepository;
	
	@Before("@annotation(HasPermission)")
	public void checkPermission(HasPermission requiresPermission) {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String requiredPermission = requiresPermission.value();

		//find user role
		Optional<Role> roleOpt = this.userRoleRepository.findRoleByUser(user);
		System.out.print(user);
		if(roleOpt.isEmpty()) {
			throw new SecurityException("User does not have the required permission");
		}

		Role role = roleOpt.get();
		Optional<Permission> permOpt =  this.permissionRepository.findByName(requiredPermission);
		if(permOpt.isPresent()) {
			var permission = permOpt.get();
			Optional<RolePermission> rolePermOpt =  rolePermissionRepository.findByRoleAndPermission(role, permission);
			if(rolePermOpt.isEmpty()) {
				throw new SecurityException("User does not have the required permission");
			}
		}
		throw new SecurityException("Invalid permission required");
	}
	

}
