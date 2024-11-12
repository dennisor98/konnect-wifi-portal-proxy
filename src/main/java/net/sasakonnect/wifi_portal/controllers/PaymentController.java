package net.sasakonnect.wifi_portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.tags.Tag;
import net.sasakonnect.wifi_portal.annotations.CustomController;
import net.sasakonnect.wifi_portal.services.PaymentService;

@CustomController
@RequestMapping("payment")
@Tag(name="Payment")
public class PaymentController {
	
	@Autowired
	PaymentService paymentService;
	
	@PostMapping("/callBack")
	public Object callBackResolver(Object request) {
		return ResponseEntity.ok(this.paymentService.mpesacallBackUrl(request));
	}
	
   @PostMapping("mpesa/stkPush")
   public Object mpesaStkPushInit() {
	   return this.paymentService.stkPush();
   }
}
