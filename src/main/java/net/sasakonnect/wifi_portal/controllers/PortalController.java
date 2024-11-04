package net.sasakonnect.wifi_portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.sasakonnect.wifi_portal.RequestDto.ChangeDeviceDto;
import net.sasakonnect.wifi_portal.RequestDto.ClientSubDto;
import net.sasakonnect.wifi_portal.RequestDto.SendOtpDto;
import net.sasakonnect.wifi_portal.RequestDto.StkPushDto;
import net.sasakonnect.wifi_portal.RequestDto.TillConfirmDto;
import net.sasakonnect.wifi_portal.RequestDto.UpdateCustomerDto;
import net.sasakonnect.wifi_portal.RequestDto.VerifyOtpDto;
import net.sasakonnect.wifi_portal.annotations.CustomController;
import net.sasakonnect.wifi_portal.services.PortalService;
import net.sasakonnect.wifi_portal.services.UserService;


@CustomController
@RequestMapping("portal")
@Tag(name="Portal")
public class PortalController extends BasePortalController{
	@Autowired
	PortalService portalService;
	
	@Autowired
	UserService userService;
	
   @PostMapping("getDevices")
   public Object getDevices() {
	   return this.portalService.getDevices();
   }
   
   @PostMapping("updateCustomer")
   public Object updateCustomerInfo(@Valid @RequestBody() UpdateCustomerDto info) {
	   return this.userService.updateCustomerInfo(info);
   }
   
   @PostMapping("getSubscriptionpackages")
   public Object getInternetPackages() {
	   return this.portalService.getInternetPackages();
   }
   
   @PostMapping("sendOTP")
   public Object sendOtp(@Valid @RequestBody() SendOtpDto otp) {
	   return this.portalService.sendOtp(otp);
   }
   
   @PostMapping("confirmOTP")
   public Object verifyOtp(@Valid @RequestBody() VerifyOtpDto otp) {
	   return this.userService.verifyOtp(otp);
   }
   
   @PostMapping("/user/getUserDetailsByPhone")
   public Object getClientDetailsByPhome() {
	   return this.userService.getUserDetailsByPhone();
   }
   
   @PostMapping("/user/getUseSubsByUserId")
   public Object getUseSubsByUserId() {
	   return this.userService.getUserDetailsByPhone();
   }
   
   @PostMapping("/user/get_client_subs")
   public Object getUserSubs() {
	   return this.portalService.getClientSubs();
   }
   
   @PostMapping("get_client_subs")
   public Object getClientSubs() {
	   return this.portalService.getClientSubs();
   }
   
   @PostMapping("changeDevice")
   public Object changeDevice(@Valid @RequestBody() ChangeDeviceDto device) {
	   return this.portalService.changeDevice(device);
   }
   
   
   @PostMapping("makeTillPayment")
   public Object confirmTillPayment(@Valid @RequestBody() TillConfirmDto confirm) {
	   return this.portalService.confirmTillPayment(confirm);
   }
   
   @PostMapping("makeSubscriptionPayment")
   public Object mpesaStkPush(@Valid @RequestBody() StkPushDto stk) {
	   return this.portalService.mpesaStkPush(stk);
   }
   
   @PostMapping("getActiveSubscriptionrenewalsByPhone")
   public Object getActiveSubscriptionrenewalsByPhone(@Valid @RequestBody SendOtpDto input) {
	   return this.portalService.getUserSubscriptionsByPhone(input.getPhone());
   }
   
   
   
}
