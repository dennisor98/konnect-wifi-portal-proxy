package net.sasakonnect.wifi_portal.controllers;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.sasakonnect.wifi_portal.RequestDto.StkPushDto;
import net.sasakonnect.wifi_portal.annotations.CustomController;
//import net.sasakonnect.wifi_portal.services.MessagingService;
//import net.sasakonnect.wifi_portal.services.MessagingService;
import net.sasakonnect.wifi_portal.services.PaymentService;

@CustomController
@RequestMapping("payment")
@Tag(name="Payment")
@Slf4j
public class PaymentController {

	@Autowired
	PaymentService paymentService;

	//	@Autowired
	//	MessagingService messageService;


	@PostMapping(value = "/callBack", produces = "application/json")
	public ResponseEntity<Map<String, String>> callBackResolver(@RequestBody(required = false) String requestBody) {
		log.info(requestBody);
		// this.paymentService.mpesacallBackUrl(requestBody);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode;
		try {
			rootNode = mapper.readTree(requestBody);
			String checkoutRequestID = rootNode.path("Body")
					.path("stkCallback")
					.path("CheckoutRequestID")
					.asText();
			this.paymentService.updatePaymentWithCheckoutId(checkoutRequestID,requestBody);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		Map<String, String> response = new HashMap<>();
		response.put("status", "success");
		response.put("message", "Callback handled successfully");
		return ResponseEntity.ok(response);
	}
	
	@PostMapping(value = "/validate", produces = "application/json")
	public Object validationCallBackResolver(@RequestBody(required = false) Object requestBody) {
		ObjectNode response = JsonNodeFactory.instance.objectNode();
		
		response.put("ResultCode", "0");
		response.put("ResultDesc", "Accepted");
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

   @PostMapping("mpesa/stkPush")
   public Object mpesaStkPushInit(@Valid @RequestBody() StkPushDto stk) {
	  // return this.paymentService.stkPush(stk);
	   return null;
   }
   
//   @PostMapping("mpesa/confirmTransaction")
//   public Object queryMpesaByChecoutRequestId(@Valid @RequestBody() PollMpesaDto stk) {
//	   return this.paymentService.getTxStatusByCheckoutRequestId(stk.getCheckoutRequestID());
//   }
//   
//   @PostMapping("mpesa/init")
//   public Object mpesaInit() {
//	   return this.messageService.processMpesaStkPush("Mpesa Messaging");
//   }
//}


//{
//	  "MerchantRequestID": "94fc-460e-a970-797968bf6a851260555",
//	  "CheckoutRequestID": "ws_CO_13112024132317657769156995",
//	  "ResponseCode": "0",
//	  "ResponseDescription": "Success. Request accepted for processing",
//	  "CustomerMessage": "Success. Request accepted for processing"
//	}