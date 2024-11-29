package net.sasakonnect.wifi_portal.services;
import net.sasakonnect.wifi_portal.domain.Payment;
import org.springframework.retry.annotation.Backoff;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.sasakonnect.wifi_portal.RequestDto.MpesaCallBackDto;
import net.sasakonnect.wifi_portal.RequestDto.PollMpesaDto;
import net.sasakonnect.wifi_portal.RequestDto.StkPushDto;
import net.sasakonnect.wifi_portal.RequestDto.sdk.MpesaResponse;
import net.sasakonnect.wifi_portal.RequestDto.sdk.PaymentRequest;
import net.sasakonnect.wifi_portal.beans.DefaultWebClientBean;
import net.sasakonnect.wifi_portal.beans.MpesaWebClientBean;
import net.sasakonnect.wifi_portal.beans.ThreadExecuterBean;
import net.sasakonnect.wifi_portal.constants.MpesaEndpointsConstants;
import net.sasakonnect.wifi_portal.domain.App;
import net.sasakonnect.wifi_portal.domain.InternetPackages;
import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.repository.AppRepository;
import net.sasakonnect.wifi_portal.repository.InternetPackageRepository;
import net.sasakonnect.wifi_portal.repository.PaymentRepository;
import reactor.core.publisher.Mono;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Retryable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;


@Service
@Slf4j
public class PaymentService {
    ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	MpesaWebClientBean mpesaClient;
	@Autowired
    public WebClient.Builder webClientBuilder;
	@Autowired
	DefaultWebClientBean webClient;

	
	@Value("${shortCode}")
	String shortCode;
//	
//	@Value("${consumerSecret}")
//	String password;
//	
	@Value("${mpesaCallBackUrl}")
	String mpesaCallBackUrl;
//	
	@Autowired
	AuthService authService;
	
	@Autowired
	MessagingService msgService;
	
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
	@Autowired
	MpesaStkPush mpesaStkPush;
	@Autowired
	AppRepository appRepository;
	
	 private final RabbitTemplate rabbitTemplate;
	    

	    public PaymentService(RabbitTemplate rabbitTemplate) {
	        this.rabbitTemplate = rabbitTemplate;
	    }
	
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
		this.createPaymentRequest(paymentRequest.getAppKey(), paymentRequest.getKonnectCheckoutID(),null);
	    System.out.println("Received Payment Request: " + paymentRequest);
	
	    threadExceutorBean.addTask(new Runnable() {

			@Override
			public void run() {
				triggerMpesaStkPush(paymentRequest);
				
			}
	    	
	    });
	}
	@RabbitListener(queues = "transactionCallBackNotificationQueue")
//	@Transactional
	public void handleTransactionCallBackNotificationQueueRequest(String paymentRequest) {
//	
	
	    threadExceutorBean.addTask(new Runnable() {

			@Override
			public void run() {
			    ObjectMapper mapper = new ObjectMapper();
		        JsonNode rootNode;
				try {
					rootNode = mapper.readTree(paymentRequest);
					 String checkoutRequestID = rootNode.path("Body")
		                     .path("stkCallback")
		                     .path("CheckoutRequestID")
		                     .asText();
					    System.out.println("Called Back called: " + checkoutRequestID);
					 var payment=   paymentRepository.findByTxtIdIgnoreCase(checkoutRequestID);
					 var paymentData = new Gson().fromJson(paymentRequest,MpesaCallBackDto.class);
					 var callback = paymentData.getBody().getStkCallback();
					 if(payment.isPresent()) {
						var pay= payment.get();
//						pay.setPaymentPayload(paymentRequest);
//						paymentRepository.save(pay)	;
						User user = pay.getUser();
						String userName = null;
						if(user!=null) {
							userName = user.getFirstname()+" "+user.getLastname();
						}
					    System.out.println("Called Back notify merchant at : " + pay.getApp().getCallbackUrl());
					    ObjectNode params =  JsonNodeFactory.instance.objectNode();
						params.put("TransType","CustomerBuyGoodsOnline");
						params.put("TransID",pay.getTxtId());
						params.put("TransTime",callback.getCallbackMetadata() !=null ? callback.getCallbackMetadata().getItem().get(3).getValue().toString():null);
						params.put("TransAmount",callback.getCallbackMetadata() !=null ? callback.getCallbackMetadata().getItem().get(0).getValue().toString():null);
						params.put("BusinessShortCode",shortCode);
						params.put("BillRefNumber", callback.getCallbackMetadata() !=null ? callback.getCallbackMetadata().getItem().get(1).getValue().toString():null);
						params.put("Mobile",callback.getCallbackMetadata() !=null ? callback.getCallbackMetadata().getItem().get(4).getValue().toString():null);
				        params.put("name", userName);
				        params.put("userId", user !=null ? user.getUserId() : null);
					    var webClient = webClientBuilder.build()
					    	    .post()
					    	    .uri(pay.getApp().getCallbackUrl())
					    	    .bodyValue(params)  // Send the payment request as the body
					    	    .retrieve()
					    	    .onStatus(
					    	        status -> !status.is2xxSuccessful(), // Check if the status is NOT 2xx (including 200)
					    	        clientResponse -> {
					    	        	// keep this job to call the client 
					    	            // Custom logic when status is NOT 2xx (i.e., not 200)
					    	            return clientResponse.bodyToMono(String.class)
					    	                    .flatMap(responseBody -> {
					    	                        // Perform any action here based on the response body or status
					    	                        return Mono.error(new RuntimeException("Payment API call failed with status: " + clientResponse.statusCode()));
					    	                    });
					    	        })
					    	    .bodyToMono(String.class) // If the status is 200, proceed with the response body
					    	    .doOnTerminate(() -> {
					    	        // Optional: Add any additional final actions after the request completes
					    	    })
					    	    .subscribe(response -> {
					    	        // Handle the response if status is 200
					    	        System.out.println("Response: " + response);
					    	    });

										
						 }else {
							 log.warn("could not find transaction for checkout id"+checkoutRequestID);
						 }
					}catch(Exception ex) {
						ex.printStackTrace();
					}

					 //this.paymentService.updatePaymentWithCheckoutId(checkoutRequestID,requestBody);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        
				
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
        rabbitTemplate.convertAndSend("transactionExchange","transaction.callbackNotification",request);

		return ResponseEntity.status(HttpStatus.OK);
	}
	

	public Object stkPush(StkPushDto stk) { 
		 User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		var phone = stk.getPhone();
		String mobile = null;
		String appKey = "f7bc83f430538424b13298e6aa6fb143efd8427454f7f9a3e49e91d90c416b0e";
		Optional<App> appOpt =  this.appRepository.findFirstByAppKeyAndAppSecret(appKey);
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
				var resp = new Gson().fromJson(responseJson,PollMpesaDto.class);
				this.msgService.processMpesaStkPush(resp);
				return resp;
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
		}
     
		
      
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		var timestamp =  LocalDateTime.now().format(format);
		ObjectNode req = JsonNodeFactory.instance.objectNode();
		var password = this.authService.getMpesaMerchantPassword(timestamp);
		log.error("password"+password);
		req.put("BusinessShortCode",stk.getApp().getBusinessShortCode());
		req.put("Password",password);
		req.put("Timestamp",timestamp);
		req.put("TransactionType","CustomerBuyGoodsOnline");
		req.put("Amount",stk.getAmount());
		req.put("PartyA","254"+mobile);
		req.put("PartyB", stk.getApp().getMpesaTillNo());
		req.put("PhoneNumber", "254"+mobile);
		req.put("CallBackURL",mpesaCallBackUrl);
		req.put("AccountReference", stk.getApp().getName());
		req.put("TransactionDesc", stk.getApp().getName()+" : ("+mobile+")");	
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
				ObjectMapper objectMapper = new ObjectMapper();
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
    public  String sendPostRequest(String url, Map<String, String> requestData) throws Exception {
        // Create a HttpClient instance
        HttpClient client = HttpClient.newHttpClient();

        // Convert Map to JSON
        String jsonPayload = buildJsonPayload(requestData);

        // Create the HttpRequest with POST method and the JSON body
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer "+authService.getMpesaAccessToken())
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();

        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Print out the response code and body
        System.out.println("Response Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
        return  response.body();
    }
    public static String buildJsonPayload(Map<String, String> requestData) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Map.Entry<String, String> entry : requestData.entrySet()) {
            sb.append("\"" + entry.getKey() + "\":\"" + entry.getValue() + "\",");
        }
        sb.deleteCharAt(sb.length() - 1);  // Remove last comma
        sb.append("}");
        return sb.toString();
    }
	private Payment createPaymentRequest(String appKey,String konnectTransactionId) {
         var currentApp= this.appService.findAppByAppKey(appKey)	;	//    	   return responseJson;
			
         if(currentApp.isPresent()){
      	   var pay=Payment.builder().app(currentApp.get())
						
						.konnectCheckoutId(konnectTransactionId) 
						.txtId(konnectTransactionId)
						.user(user)
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
