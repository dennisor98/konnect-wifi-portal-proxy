package net.sasakonnect.wifi_portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.sasakonnect.wifi_portal.annotations.PaymentSdkFilter;
import net.sasakonnect.wifi_portal.services.PortalService;

@RequestMapping("utility")
@Tag(name="Utility")
@RestController
public class UtilityController {
	@Autowired
	PortalService portalService;
	
	@PostMapping("userSubs")
	@ApiOperation(value = "Request to get user subscriptions", notes = "This endpoint gets user subriptions to packages including the respective devices added to the packages")
	@PaymentSdkFilter
   public Object getUserSubscriptions(
		   @RequestHeader(value = "App-Key") 
		   @Parameter(description = "The App Key used for authentication", 
			 required = true, 
			 example = "f7bc83f430538424b13298e6aa6fb143efd8427454f7f9a3e49e91d90c416b0e") 
			 String appKey,

			 @RequestHeader(value = "App-Secret") 
			 @Parameter(description = "The App Secret used for authentication", 
			 required = true, 
			 example = "1d8d6cf2c65c0f2875e6b79f675bd1e5ad3b90f9b4e18f649134d8f5c8f94e7d") 
			 String appSecret,
			 
		    @RequestParam(name="mobileNumber",required = true) String phone
		   ) {
	   return this.portalService.getUserSubscriptionsByUserId(phone);
	}
}
