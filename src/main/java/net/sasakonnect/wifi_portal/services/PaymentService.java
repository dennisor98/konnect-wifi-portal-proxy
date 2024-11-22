package net.sasakonnect.wifi_portal.services;
import net.sasakonnect.wifi_portal.domain.Payment;
import org.springframework.retry.annotation.Backoff;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.sasakonnect.wifi_portal.RequestDto.StkPushDto;
import net.sasakonnect.wifi_portal.RequestDto.sdk.MpesaResponse;
import net.sasakonnect.wifi_portal.RequestDto.sdk.PaymentRequest;
import net.sasakonnect.wifi_portal.beans.MpesaWebClientBean;
import net.sasakonnect.wifi_portal.beans.ThreadExecuterBean;
import net.sasakonnect.wifi_portal.constants.MpesaEndpointsConstants;
import net.sasakonnect.wifi_portal.domain.App;
import net.sasakonnect.wifi_portal.domain.InternetPackages;
import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.repository.InternetPackageRepository;
import net.sasakonnect.wifi_portal.repository.PaymentRepository;
import reactor.core.publisher.Mono;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Retryable;
@Service
@Slf4j
public class PaymentService {
    ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	MpesaWebClientBean mpesaClient;
	
	@Value("${shortCode}")
	String shortCode;
	
	@Value("${consumerSecret}")
	String password;
	
	@Value("${mpesaCallBackUrl}")
	String mpesaCallBackUrl;
	
	@Autowired
	AuthService authService;
	
	@Autowired
	InternetPackageRepository internetPackageRepository;
	
	@Autowired
	PaymentRepository paymentRepository;
	@Autowired
	AppService appService;
	@Autowired 
	RabbitMqSenderService rabitMqSenderService;
	@Autowired
	ThreadExecuterBean threadExceutorBean;
    private Payment saveOrUpdatePayment(PaymentRequest payment,App app,String mpesaCheckoutId) {
        // Check if a payment with the same konnectCheckoutId exists
        Optional<Payment> existingPayment = paymentRepository.findByKonnectCheckoutId(payment.getKonnectCheckoutID());
        
        if (existingPayment.isPresent()) {
            // If it exists, update the payment (you can modify the payment fields as needed)
            Payment existing = existingPayment.get();
            existing.setTxtId(mpesaCheckoutId);

				
            existing.setIsSuccessful(false);
            existing.setVerified(false);
            return paymentRepository.save(existing);
        }
        return null;
    }
    private Payment saveOrUpdatePaymentMessage(String message, String konnectCheckoutId) {
        // Check if a payment with the same konnectCheckoutId exists
        Optional<Payment> existingPayment = paymentRepository.findByKonnectCheckoutId(konnectCheckoutId);
        
        if (existingPayment.isPresent()) {
            // If it exists, update the payment (you can modify the payment fields as needed)
            Payment existing = existingPayment.get();
              existing.setVerified(true);
              existing.setPaymentPayload(message);
              
				
            
            return paymentRepository.save(existing);
        }
        return null;
    }
	
	@RabbitListener(queues = "paymentRequestQueue")
	public void handlePaymentRequest(PaymentRequest paymentRequest) {
		this.createPaymentRequest(paymentRequest.getAppKey(), paymentRequest.getKonnectCheckoutID());
	    System.out.println("Received Payment Request: " + paymentRequest);
	    threadExceutorBean.addTask(new Runnable() {

			@Override
			public void run() {
			    triggerMpesaStkPush(paymentRequest);
				
			}
	    	
	    });
	}
	@RabbitListener(queues = "checkOutIdConfirmationQueue")
	public void checkOutIDConfirmationQueue(PaymentRequest paymentRequest,Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long tag) {
	    threadExceutorBean.addTask(new Runnable() {
          int count=0;
			@Override
			public void run() {
				try {
					
						   log.error("THE COUNT "+count);
			               log.info("Start Polling this"+paymentRequest);	
			           	Thread.sleep(20000);
                            MpesaResponse results=getTxStatusByCheckoutRequestId(paymentRequest.getExternalCheckoutId());
			               saveOrUpdatePaymentMessage(results.toString(),paymentRequest.getKonnectCheckoutID());
		            		//channel.basicAck(tag, false);
		                   log.info("Polling results"+results);		

					
					
				

				}  catch (Exception e) {

				}
					
			}
	    	
	    });
	}
	public Object mpesacallBackUrl(Object request){
		log.error("{callBack} "+request);
		return ResponseEntity.status(HttpStatus.OK);
	}
	public Object stkPush(StkPushDto stk) { 
		 User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		var phone = stk.getPhone();
		String mobile = null;
		if(phone !=null){
			if(phone.trim().length() < 9) {
				ObjectNode node = JsonNodeFactory.instance.objectNode();
				node.put("success", false);
				node.put("message","Phone number must be at least 9 digits");

				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(node);
			}
			
			mobile = phone.trim().substring(phone.length() -9);
		}else {
			var userphone = user.getPhone().trim();
			mobile = userphone.substring(userphone.length() -9 );
		}
     
		
      
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		var timestamp =  LocalDateTime.now().format(format);
		ObjectNode req = JsonNodeFactory.instance.objectNode();
		var password = this.authService.getMpesaMerchantPassword(timestamp);
		req.put("BusinessShortCode",shortCode);
		req.put("Password",password);
		req.put("Timestamp",timestamp);
		req.put("TransactionType","CustomerPayBillOnline");
		req.put("Amount","1");
		req.put("PartyA","254"+mobile);
		req.put("PartyB", shortCode);
		req.put("PhoneNumber", "254"+mobile);
		req.put("CallBackURL",mpesaCallBackUrl);
		req.put("AccountReference", "Test");
		req.put("TransactionDesc", "Test");	
			log.error(req+"{req}");
			Mono<String> responseMono = this.mpesaClient.webClient
			        .post()
			        .uri(MpesaEndpointsConstants.STK_PUSH)
			        .header("Authorization","Bearer "+ this.authService.getMpesaAccessToken())
			        .contentType(MediaType.APPLICATION_JSON)
			        .body(BodyInserters.fromValue(req))
			        .accept(MediaType.APPLICATION_JSON)
			        .retrieve()  
			        .bodyToMono(String.class);

		try {
			String responseJson = responseMono.block();
			if(responseJson !=null) {
				//    	   return responseJson;
				return new Gson().fromJson(responseJson,Map.class);
			}
			
		}catch(Exception ex) {
			ex.printStackTrace();
			ObjectNode node = JsonNodeFactory.instance.objectNode();
			node.put("success",false);
			node.put("message","An error ocurred");
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(node);
		}

		return null;
	}
	
	private String getSubscriptionCostById(String id) {
		Optional<InternetPackages> packageOpt = this.internetPackageRepository.findByForeignPackageId(id);
		if(packageOpt.isPresent()) {
			var pkg = packageOpt.get();
			return String.valueOf(pkg.getCost());
		}
		return "0";
	}
	
	public MpesaResponse getTxStatusByCheckoutRequestId(String checkoutRequestId) {
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		var timestamp =  LocalDateTime.now().format(format);
		ObjectNode body = JsonNodeFactory.instance.objectNode();
		body.put("BusinessShortCode",shortCode);
		body.put("Password",this.authService.getMpesaMerchantPassword(timestamp));
		body.put("Timestamp", timestamp);
		body.put("CheckoutRequestID",checkoutRequestId);
		log.info("body"+body.toPrettyString());
		
		Mono<String> responseMono = this.mpesaClient.webClient
				.post()
				.uri(MpesaEndpointsConstants.TX_QUERY)
				.header("Authorization","Bearer "+ this.authService.getMpesaAccessToken())
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(body))
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()

				.bodyToMono(String.class);

		try {
			String responseJson = responseMono.block();
			if(responseJson !=null) {
				return new Gson().fromJson(responseJson,MpesaResponse.class);
			}
			
		}catch(Exception ex) {
			ex.printStackTrace();
			ObjectNode node = JsonNodeFactory.instance.objectNode();
			node.put("success",false);
			node.put("message","An error ocurred");
			return null;
			
		}
		

		return null;
	}
	public Object triggerMpesaStkPush(@Valid PaymentRequest stk) {
		var appKey=stk.getAppKey();
		var phone = stk.getPhoneNumber();
		String mobile = null;
		if(phone !=null){
			if(phone.trim().length() < 9) {
				ObjectNode node = JsonNodeFactory.instance.objectNode();
				node.put("success", false);
				node.put("message","Phone number must be at least 9 digits");

				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(node);
			}
			
			mobile = phone.trim().substring(phone.length() -9);
		}else {
			return null;
		}
     
		
      
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		var timestamp =  LocalDateTime.now().format(format);
		ObjectNode req = JsonNodeFactory.instance.objectNode();
		var password = this.authService.getMpesaMerchantPassword(timestamp);
		log.error("password"+password);
		req.put("BusinessShortCode",shortCode);
		req.put("Password",password);
		req.put("Timestamp",timestamp);
		req.put("TransactionType","CustomerPayBillOnline");
		req.put("Amount","1");
		req.put("PartyA","254"+mobile);
		req.put("PartyB", shortCode);
		req.put("PhoneNumber", "254"+mobile);
		req.put("CallBackURL",mpesaCallBackUrl);
		req.put("AccountReference", "Test");
		req.put("TransactionDesc", "Test");	
			log.error(req+"{req}");
			Mono<String> responseMono = this.mpesaClient.webClient
			        .post()
			        .uri(MpesaEndpointsConstants.STK_PUSH)
			        .header("Authorization","Bearer "+ this.authService.getMpesaAccessToken())
			        .contentType(MediaType.APPLICATION_JSON)
			        .body(BodyInserters.fromValue(req))
			        .accept(MediaType.APPLICATION_JSON)
			        .retrieve()  
			        .bodyToMono(String.class);

		try {
			String responseJson = responseMono.block();
			
			if(responseJson !=null) {
	            
	            // Deserialize JSON into MpesaResponse object
	            MpesaResponse response = objectMapper.readValue(responseJson, MpesaResponse.class);
	           var currentApp= this.appService.findAppByAppKey(appKey)	;	//    	   return responseJson;
				
	           if(currentApp.isPresent()){

	       				this.saveOrUpdatePayment(stk,currentApp.get(),response.getCheckoutRequestID());
	       				stk.setExternalCheckoutId(response.getCheckoutRequestID());
	    				this.rabitMqSenderService.sendMpesaCheckoutRequestId(stk);
	       				
	       				return new Gson().fromJson(responseJson,Map.class);
	           }
	           
			}
			
		}catch(Exception ex) {
			ex.printStackTrace();
			ObjectNode node = JsonNodeFactory.instance.objectNode();
			node.put("success",false);
			node.put("message","An error ocurred");
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(node);
		}

		return null;
	}
	private Payment createPaymentRequest(String appKey,String konnectTransactionId) {
         var currentApp= this.appService.findAppByAppKey(appKey)	;	//    	   return responseJson;
			
         if(currentApp.isPresent()){
      	   var pay=Payment.builder().app(currentApp.get())
						
						.konnectCheckoutId(konnectTransactionId)     				
     				.isSuccessful(false)
     				.verified(false).build()			;
     				return this.paymentRepository.save(pay);
     				
     			
     				
         }
         return null;
	}
	public Object triggerMpesaStkPush(@Valid PaymentRequest stk, App currentApp) {
		var phone = stk.getPhoneNumber();
		String mobile = null;
		if(phone !=null){
			if(phone.trim().length() < 9) {
				ObjectNode node = JsonNodeFactory.instance.objectNode();
				node.put("success", false);
				node.put("message","Phone number must be at least 9 digits");

				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(node);
			}
			
			mobile = phone.trim().substring(phone.length() -9);
		}else {
			return null;
		}
     
		
      
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		var timestamp =  LocalDateTime.now().format(format);
		ObjectNode req = JsonNodeFactory.instance.objectNode();
		var password = this.authService.getMpesaMerchantPassword(timestamp);
		log.error("password"+password);
		req.put("BusinessShortCode",shortCode);
		req.put("Password",password);
		req.put("Timestamp",timestamp);
		req.put("TransactionType","CustomerPayBillOnline");
		req.put("Amount","1");
		req.put("PartyA","254"+mobile);
		req.put("PartyB", shortCode);
		req.put("PhoneNumber", "254"+mobile);
		req.put("CallBackURL",mpesaCallBackUrl);
		req.put("AccountReference", "Test");
		req.put("TransactionDesc", "Test");	
			log.error(req+"{req}");
			Mono<String> responseMono = this.mpesaClient.webClient
			        .post()
			        .uri(MpesaEndpointsConstants.STK_PUSH)
			        .header("Authorization","Bearer "+ this.authService.getMpesaAccessToken())
			        .contentType(MediaType.APPLICATION_JSON)
			        .body(BodyInserters.fromValue(req))
			        .accept(MediaType.APPLICATION_JSON)
			        .retrieve()  
			        .bodyToMono(String.class);

		try {
			String responseJson = responseMono.block();
			
			if(responseJson !=null) {
	            
	            // Deserialize JSON into MpesaResponse object
	            MpesaResponse response = objectMapper.readValue(responseJson, MpesaResponse.class);
				//    	   return responseJson;
				var pay=Payment.builder().app(currentApp)
				.txtId(response.getCheckoutRequestID())
				.isSuccessful(false)
				.verified(false).build()			;
				this.paymentRepository.save(pay);
				stk.setExternalCheckoutId(response.getCheckoutRequestID());
				this.rabitMqSenderService.sendMpesaCheckoutRequestId(stk);
				
				return new Gson().fromJson(responseJson,Map.class);
			}
			
		}catch(Exception ex) {
			ex.printStackTrace();
			ObjectNode node = JsonNodeFactory.instance.objectNode();
			node.put("success",false);
			node.put("message","An error ocurred");
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(node);
		}

		return null;
	}
	
}
