package net.sasakonnect.wifi_portal.config;



import java.io.Serializable;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import net.sasakonnect.wifi_portal.domain.Role;
import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.services.UserService;



@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
	@Autowired
	private UserService userService;

	public CustomPermissionEvaluator(UserService userService) {
		this.userService = userService;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
//		System.out.print("Check permission for " + authentication.getPrincipal() + "For target domain object"
//				+ targetDomainObject + " for permission " + permission);
		System.out.print("check error");
		Optional<Role> role = this.userService
				.getUserRoleByUserId(((User) authentication.getPrincipal()).getId().toString());
		if (role.isEmpty() && permission == null) {
			return true;
		}
		if (role.isEmpty() && permission != null) {
			return false;
		}
		System.out.print(permission);
		if (role.isPresent() && this.userService.findPermissionByRoleName(role, permission)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
			Object permission) {
		System.out.println("Hello there ");

		// This method can be implemented similarly to the previous one, if needed
		return true;
	}
}

