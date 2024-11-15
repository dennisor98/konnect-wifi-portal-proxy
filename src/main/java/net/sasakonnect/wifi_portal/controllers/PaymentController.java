package net.sasakonnect.wifi_portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.sasakonnect.wifi_portal.RequestDto.PollMpesaDto;
import net.sasakonnect.wifi_portal.RequestDto.StkPushDto;
import net.sasakonnect.wifi_portal.RequestDto.TillConfirmDto;
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
   public Object mpesaStkPushInit(@Valid @RequestBody() StkPushDto stk) {
	   return this.paymentService.stkPush(stk);
   }
   
   @PostMapping("mpesa/confirmTransaction")
   public Object queryMpesaByChecoutRequestId(@Valid @RequestBody() PollMpesaDto stk) {
	   return this.paymentService.getTxStatusByCheckoutRequestId(stk.getCheckoutRequestID());
   }
}


//{
//	  "MerchantRequestID": "94fc-460e-a970-797968bf6a851260555",
//	  "CheckoutRequestID": "ws_CO_13112024132317657769156995",
//	  "ResponseCode": "0",
//	  "ResponseDescription": "Success. Request accepted for processing",
//	  "CustomerMessage": "Success. Request accepted for processing"
//	}