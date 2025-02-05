package net.sasakonnect.wifi_portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.sasakonnect.wifi_portal.RequestDto.ProfileUploadDto;
import net.sasakonnect.wifi_portal.RequestDto.sdk.UserLoginDto;
import net.sasakonnect.wifi_portal.annotations.CustomController;
import net.sasakonnect.wifi_portal.services.UserService;

@CustomController
@RequestMapping("user")
@Tag(name="User")
public class UserController {
    @Autowired
    UserService userService;
	@PostMapping("/profile")
	public Object uploadProfileImage(@Valid @RequestBody() ProfileUploadDto profile) {
		return this.userService.uploadProfileImage(profile);
	}
	
	
	@GetMapping("/profile")
	public Object getuserprofile() {
		return this.userService.getAuthenticatedUserProfile();
	}
	
	@PostMapping("/login")
	public Object login(@Valid @RequestBody() UserLoginDto logins) {
		return this.userService.adminUserLogin(logins);
	}
}
