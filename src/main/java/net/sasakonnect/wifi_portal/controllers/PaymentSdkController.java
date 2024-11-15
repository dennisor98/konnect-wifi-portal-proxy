package net.sasakonnect.wifi_portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.sasakonnect.wifi_portal.RequestDto.StkPushDto;
import net.sasakonnect.wifi_portal.services.PaymentService;

@RequestMapping("sdk")
@Tag(name="Portal")
public class PaymentSdkController {
	@Autowired
	PaymentService paymentService;
	 @PostMapping("mpesa/stkPush")
	   public Object mpesaStkPushInit(@Valid @RequestBody() StkPushDto stk) {
		   return this.paymentService.stkPush(stk);
	   }

}
