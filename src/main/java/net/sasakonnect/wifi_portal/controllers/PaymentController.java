package net.sasakonnect.wifi_portal.controllers;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
import net.sasakonnect.wifi_portal.RequestDto.AppToolKitPayDto;
import net.sasakonnect.wifi_portal.RequestDto.MpesaPaymentValidationDto;
import net.sasakonnect.wifi_portal.RequestDto.MpesaResultDto;
import net.sasakonnect.wifi_portal.RequestDto.PollTxStatusDto;
import net.sasakonnect.wifi_portal.RequestDto.StkPushDto;
import net.sasakonnect.wifi_portal.RequestDto.ToolkitPayDto;
import net.sasakonnect.wifi_portal.ResponseDto.StkCallbackResponseDTO;
import net.sasakonnect.wifi_portal.annotations.CustomController;
import net.sasakonnect.wifi_portal.annotations.RateLimit;
import net.sasakonnect.wifi_portal.beans.ThreadExecuterBean;
import net.sasakonnect.wifi_portal.domain.Payment;
import net.sasakonnect.wifi_portal.repository.PaymentRepository;
import net.sasakonnect.wifi_portal.services.AuthService;
//import net.sasakonnect.wifi_portal.services.MessagingService;
//import net.sasakonnect.wifi_portal.services.MessagingService;
import net.sasakonnect.wifi_portal.services.PaymentService;
import net.sasakonnect.wifi_portal.services.RabbitMqSenderService;
import net.sasakonnect.wifi_portal.services.RedisService;

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
	@Autowired
	RedisService redisService;
	@Autowired
	PaymentRepository paymentRepository;
	@Autowired
	ThreadExecuterBean threadExceutorBean;
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
		log.error("{stkCallBack}"+requestBody);

		var callBackPayLoad = new Gson().fromJson(requestBody,StkCallbackResponseDTO.class);
		log.error("{gson}"+callBackPayLoad);
		var stkCall = callBackPayLoad.getBody().getStkCallback();
		String checkoutReqId = stkCall.getCheckoutRequestID();
		this.paymentService.updatePaymentWithCheckoutId(checkoutReqId,requestBody);
		this.paymentService.processMpesaCallBack(callBackPayLoad);
		Map<String, String> response = new HashMap<>();
		response.put("status", "success");
		response.put("message", "Callback handled successfully");
		return ResponseEntity.ok(response);
	}

	@PostMapping("mpesa/stkPush")
	public Object mpesaStkPushInit(@Valid @RequestBody() StkPushDto stk) {
		return this.paymentService.stkPush(stk);
	}


   
   @GetMapping("mpesa/confirmTransaction")
   public void queryMpesaByChecoutRequestId() {
	    this.redisService.addTransactionIten("hello");
   }
//   
   @PostMapping("/result")
   @RateLimit(maxRequests = 100, durationSeconds = 60)
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
   public Object requestToolkitPayment(@Valid @RequestBody AppToolKitPayDto payReq) {
	   return this.paymentService.createPaymentRequest(payReq);
   }
   
   @PostMapping("/pollPaymentStatusByTxId")
   public Object pollPaymentStatus(@Valid @RequestBody PollTxStatusDto payReq) {
	   return this.paymentService.getPaymentStatusByTxId(payReq);
   }
   
   @PostMapping("options")
   public Object getPaymentMethods() {
	   return this.paymentService.getPaymentMethods();
   }
   
   
}

