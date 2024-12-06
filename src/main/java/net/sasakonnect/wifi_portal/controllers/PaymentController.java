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
import com.google.gson.Gson;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.sasakonnect.wifi_portal.RequestDto.MpesaPaymentValidationDto;
import net.sasakonnect.wifi_portal.RequestDto.MpesaResultDto;
import net.sasakonnect.wifi_portal.RequestDto.StkPushDto;
import net.sasakonnect.wifi_portal.RequestDto.ToolkitPayDto;
import net.sasakonnect.wifi_portal.annotations.CustomController;
import net.sasakonnect.wifi_portal.services.AuthService;
//import net.sasakonnect.wifi_portal.services.MessagingService;
//import net.sasakonnect.wifi_portal.services.MessagingService;
import net.sasakonnect.wifi_portal.services.PaymentService;
import net.sasakonnect.wifi_portal.services.RabbitMqSenderService;

@CustomController
@RequestMapping("payment")
@Tag(name="Payment")
@Slf4j
public class PaymentController {

	@Autowired
	PaymentService paymentService;
	@Autowired
	AuthService authService;
	@Autowired
	RabbitMqSenderService rabitMqSenderService;

	//	@Autowired
	//	MessagingService messageService;


	@PostMapping(value = "/callBack", produces = "application/json")
	public Object paymentValidationCallBack(@RequestBody(required = false) MpesaPaymentValidationDto requestBody) {
		return this.paymentService.validatePayment(requestBody);
	}
	
	@PostMapping(value = "/confirm", produces = "application/json")
	public Object paymentConfirmationCallBack(@RequestBody(required = false) MpesaPaymentValidationDto requestBody) {
		log.info("{request}"+ requestBody);
		// this.paymentService.mpesacallBackUrl(requestBody);
         ObjectNode response = JsonNodeFactory.instance.objectNode();
		response.put("ResultCode", "0");
		response.put("ResultDesc", "Accepted");
		this.rabitMqSenderService.requestPaymentStatus(requestBody);
		return ResponseEntity.status(HttpStatus.OK);
	}
	
	@PostMapping(value = "/callbackResolver", produces = "application/json")
	public Object validationCallBackResolver(@RequestBody(required = false) String requestBody) {
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

	@PostMapping("mpesa/stkPush")
	public Object mpesaStkPushInit(@Valid @RequestBody() StkPushDto stk) {
		return this.paymentService.stkPush(stk);
	}


//   
//   @PostMapping("mpesa/confirmTransaction")
//   public Object queryMpesaByChecoutRequestId(@Valid @RequestBody() PollMpesaDto stk) {
//	   return this.paymentService.getTxStatusByCheckoutRequestId(stk.getCheckoutRequestID());
//   }
//   
   @PostMapping("/result")
   public void mpesaInit(@Valid @RequestBody() String result) throws Exception {
	  log.info("{result}"+result);
	  try {
		  var payment = new Gson().fromJson(result,MpesaResultDto.class);
		  log.info("{result}"+payment);
		   this.paymentService.processMpesaStatusResult(payment);
	  }catch(Exception ex) {
		  log.error("failed to serialize");
		  ex.printStackTrace();
	  }
	  
   }
   
   @PostMapping("/toolkitPay")
   public Object requestToolkitPayment(@Valid @RequestBody ToolkitPayDto payReq) {
	   return this.paymentService.createMerchantPaymentRequest(payReq);
   }
}

