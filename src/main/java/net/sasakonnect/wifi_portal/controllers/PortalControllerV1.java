package net.sasakonnect.wifi_portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.sasakonnect.wifi_portal.RequestDto.StkPushDtoV1;
import net.sasakonnect.wifi_portal.annotations.CustomController;
import net.sasakonnect.wifi_portal.services.PortalService;

@CustomController
@RequestMapping("portal/v1")
@Tag(name="Portal V1")
public class PortalControllerV1 {
	@Autowired
	PortalService portalService;
	
	@PostMapping("makeSubscriptionPayment")
	public Object mpesaStkPush(@Valid @RequestBody() StkPushDtoV1 stk) {
		return this.portalService.mpesaStkPush(stk);
	}
	
	@PostMapping("activateSub")
	public Object activateSub(
			@RequestParam(name="subId") String subId
			) {
		return this.portalService.activateSub(subId);
	}
	   
}
