package net.sasakonnect.wifi_portal.services;

import net.sasakonnect.wifi_portal.domain.Payment;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
import net.sasakonnect.wifi_portal.RequestDto.MpesaPaymentValidationDto;
import net.sasakonnect.wifi_portal.RequestDto.MpesaResultDto;
import net.sasakonnect.wifi_portal.RequestDto.PollMpesaDto;
import net.sasakonnect.wifi_portal.RequestDto.StkPushDto;
import net.sasakonnect.wifi_portal.RequestDto.ToolkitPayDto;
import net.sasakonnect.wifi_portal.RequestDto.sdk.MpesaResponse;
import net.sasakonnect.wifi_portal.RequestDto.sdk.PaymentRequest;
import net.sasakonnect.wifi_portal.beans.AdvancedUniqueKeyGenerator;
import net.sasakonnect.wifi_portal.beans.DefaultWebClientBean;
import net.sasakonnect.wifi_portal.beans.MpesaWebClientBean;
import net.sasakonnect.wifi_portal.beans.PackagePricesBean;
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

//	@Autowired
//	MessagingService msgService;

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
	@Autowired
	PackagePricesBean packageBean;
	@Value("${mpesa.initiator}")
	String initiator;
	@Value("${mpesa.password}")
	String mpesaPassword;
	@Value("${mpesa.business.shortcode}")
	String businessShortCode;

	private final RabbitTemplate rabbitTemplate;

	public PaymentService(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	private Payment saveOrUpdatePayment(PaymentRequest payment, App app, String mpesaCheckoutId) {
		// Check if a payment with the same konnectCheckoutId exists
		Optional<Payment> existingPayment = paymentRepository.findByKonnectCheckoutId(payment.getKonnectCheckoutID());

		if (existingPayment.isPresent()) {
			// If it exists, update the payment (you can modify the payment fields as
			// needed)
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
			// If it exists, update the payment (you can modify the payment fields as
			// needed)
			Payment existing = existingPayment.get();
			existing.setVerified(true);
			existing.setPaymentPayload(message);
			return paymentRepository.save(existing);
		}
		return null;
	}

	@RabbitListener(queues = "paymentRequestQueue")
	public void handlePaymentRequest(String jsonPayload) {
		try {
			PaymentRequest paymentRequest = new ObjectMapper().readValue(jsonPayload, PaymentRequest.class);
			System.out.println("Received Payment Request: " + paymentRequest);

			this.createPaymentRequest(paymentRequest.getAppKey(), paymentRequest.getKonnectCheckoutID(), null);

			threadExceutorBean.addTask(new Runnable() {
				@Override
				public void run() {
					triggerMpesaStkPush(paymentRequest);
				}
			});
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
	
//	
	@RabbitListener(queues = "transactionCallBackNotificationQueue")
	@Transactional
	public void handleTransactionCallBackNotificationQueueRequest(String paymentRequest) {
//	
		log.error("payment callback" + paymentRequest);

		threadExceutorBean.addTask(new Runnable() {

			@Override
			public void run() {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode rootNode;
				try {
					rootNode = mapper.readTree(paymentRequest);
					String checkoutRequestID = rootNode.path("Body").path("stkCallback").path("CheckoutRequestID")
							.asText();
					System.out.println("Called Back called: " + paymentRequest);
					System.out.println("what happened");
					try {
						Optional<Payment> payment = paymentRepository.findByTxtIdIgnoreCase(checkoutRequestID);
						log.error("" + payment);

						var paymentData = new Gson().fromJson(paymentRequest, MpesaCallBackDto.class);
						var callback = paymentData.getBody().getStkCallback();
						if (payment.isPresent()) {
							var pay = payment.get();
//							pay.setPaymentPayload(paymentRequest);
//							paymentRepository.save(pay)	;
							User user = pay.getUser();
							String userName = null;
							if (user != null) {
								userName = user.getFirstname() + " " + user.getLastname();
							}
							System.out.println("Called Back notify merchant at : " + pay.getApp().getCallbackUrl());
							Map<String, Object> params = new HashMap<>();
							params.put("TransType", "CustomerBuyGoodsOnline");
							params.put("TransID", pay.getTxtId());
							params.put("TransTime",
									callback.getCallbackMetadata() != null
											? callback.getCallbackMetadata().getItem().get(3).getValue()
											: null);
							params.put("TransAmount",
									callback.getCallbackMetadata() != null
											? callback.getCallbackMetadata().getItem().get(0).getValue()
											: null);
							params.put("BusinessShortCode", shortCode);
							params.put("BillRefNumber",
									callback.getCallbackMetadata() != null
											? callback.getCallbackMetadata().getItem().get(1).getValue()
											: null);
							params.put("Mobile",
									callback.getCallbackMetadata() != null
											? String.valueOf(callback.getCallbackMetadata().getItem().get(4).getValue())
											: null);
							params.put("name", userName);
							params.put("userId", user != null ? user.getUserId() : null);

							log.error("{body}" + new Gson().toJson(params));
							Mono<Object> respMono = webClient.webClient.post().uri(pay.getApp().getCallbackUrl())
									.contentType(MediaType.APPLICATION_JSON)
									.body(BodyInserters.fromValue(new Gson().toJson(params)))
									.accept(MediaType.APPLICATION_JSON).retrieve()
//					                .onStatus(
//					                    status -> !status.is2xxSuccessful(), 
//					                    clientResponse -> clientResponse.bodyToMono(String.class)
//					                            .flatMap(responseBody -> {
//					                                // Custom logic when status is NOT 2xx
//					                                System.out.println("Error response body: " + responseBody);
//					                                return Mono.error(new RuntimeException(
//					                                        "Payment API call failed with status: " + clientResponse.statusCode()));
//					                            })
//					                )
									.bodyToMono(Object.class);
							respMono.block();
//					                .doOnTerminate(() -> {
//					                    // Optional: Add any additional final actions after the request completes
//					                    System.out.println("Request completed");
//					                })
//					                .subscribe(response -> {
//					                    // Handle the response if status is 2xx
//					                    System.out.println("Response: " + response);
//					                });

						} else {
							log.warn("could not find transaction for checkout id" + checkoutRequestID);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}

					// this.paymentService.updatePaymentWithCheckoutId(checkoutRequestID,requestBody);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		});
	}

	@RabbitListener(queues = "checkOutIdConfirmationQueue")
	public void checkOutIDConfirmationQueue(PaymentRequest paymentRequest, Channel channel,
			@Header(AmqpHeaders.DELIVERY_TAG) long tag) {
		threadExceutorBean.addTask(new Runnable() {
			int count = 0;

			@Override
			public void run() {
				try {

					log.error("THE COUNT " + count);
					log.info("Start Polling this" + paymentRequest);
					Thread.sleep(20000);
					MpesaResponse results = getTxStatusByCheckoutRequestId(paymentRequest.getExternalCheckoutId());
					saveOrUpdatePaymentMessage(results.toString(), paymentRequest.getKonnectCheckoutID());
					// channel.basicAck(tag, false);
					log.info("Polling results" + results);

				} catch (Exception e) {

				}

			}

		});
	}
//	public Object mpesacallBackUrl(Object request){
//		log.error("{callBack} "+request);
//        rabbitTemplate.convertAndSend("transactionExchange","transaction.callbackNotification",request);
//
//		return ResponseEntity.status(HttpStatus.OK);
//	}
//	
	
	

	public Object stkPush(StkPushDto stk) {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		var phone = stk.getPhone();
		String mobile = null;
		String appKey = "f7bc83f430538424b13298e6aa6fb143efd8427454f7f9a3e49e91d90c416b0e";
		Optional<App> appOpt = this.appRepository.findFirstByAppKeyAndAppSecret(appKey);
		if (phone != null) {
			if (phone.trim().length() < 9) {
				ObjectNode node = JsonNodeFactory.instance.objectNode();
				node.put("success", false);
				node.put("message", "Phone number must be at least 9 digits");

				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(node);
			}

			mobile = phone.trim().substring(phone.length() - 9);
		} else {
			var userphone = user.getPhone().trim();
			mobile = userphone.substring(userphone.length() - 9);
		}

		if (appOpt.isPresent()) {
			App app = appOpt.get();
//			this.getSubscriptionCostById(stk.getSubscriptionPlanId())

		    var payReq = PaymentRequest.builder().phoneNumber("254"+mobile).app(app).appKey(appKey).amount(1).build();
			log.info("{payReq}"+payReq);

			return this.rabitMqSenderService.sendPaymentRequest(payReq);

		}

		return null;
	}

	public Integer getSubscriptionCostById(String id) {
		Optional<InternetPackages> packageOpt = this.internetPackageRepository.findByForeignPackageId(id);
		if (packageOpt.isPresent()) {
			var pkg = packageOpt.get();
			return pkg.getCost();
		}
		return 0;
	}

	public MpesaResponse getTxStatusByCheckoutRequestId(String checkoutRequestId) {
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		var timestamp = LocalDateTime.now().format(format);
		ObjectNode body = JsonNodeFactory.instance.objectNode();
		body.put("BusinessShortCode", shortCode);
		body.put("Password", this.authService.getMpesaMerchantPassword(timestamp));
		body.put("Timestamp", timestamp);
		body.put("CheckoutRequestID", checkoutRequestId);
		log.info("body" + body.toPrettyString());

		Mono<String> responseMono = this.mpesaClient.webClient.post().uri(MpesaEndpointsConstants.TX_QUERY)
				.header("Authorization", "Bearer " + this.authService.getMpesaAccessToken())
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(body))
				.accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(String.class);

		try {
			String responseJson = responseMono.block();
			if (responseJson != null) {
				return new Gson().fromJson(responseJson, MpesaResponse.class);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			ObjectNode node = JsonNodeFactory.instance.objectNode();
			node.put("success", false);
			node.put("message", "An error ocurred");
			return null;

		}

		return null;
	}

	public Object triggerMpesaStkPush(@Valid PaymentRequest stk) {
		var appKey = stk.getAppKey();
		var phone = stk.getPhoneNumber();
		String mobile = null;
		if (phone != null) {
			if (phone.trim().length() < 9) {
				ObjectNode node = JsonNodeFactory.instance.objectNode();
				node.put("success", false);
				node.put("message", "Phone number must be at least 9 digits");

				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(node);
			}

			mobile = phone.trim().substring(phone.length() - 9);
		}

		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		var timestamp = LocalDateTime.now().format(format);
		ObjectNode req = JsonNodeFactory.instance.objectNode();
		var password = this.authService.getMpesaMerchantPassword(timestamp);
		log.error("password" + password);
		req.put("BusinessShortCode", stk.getApp().getBusinessShortCode());
		req.put("Password", password);
		req.put("Timestamp", timestamp);
		req.put("TransactionType", "CustomerBuyGoodsOnline");
		req.put("Amount", stk.getAmount());
		req.put("PartyA", "254" + mobile);
		req.put("PartyB", stk.getApp().getMpesaTillNo());
		req.put("PhoneNumber", "254" + mobile);
		req.put("CallBackURL", mpesaCallBackUrl);
		req.put("AccountReference", stk.getApp().getName());
		req.put("TransactionDesc", stk.getApp().getName() + " : (" + mobile + ")");
		log.error(req + "{req}");
		Mono<String> responseMono = this.mpesaClient.webClient.post().uri(MpesaEndpointsConstants.STK_PUSH)
				.header("Authorization", "Bearer " + this.authService.getMpesaAccessToken())
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(req))
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);

		try {
			String responseJson = responseMono.block();
			log.error("response" + responseJson);
			if (responseJson != null) {

				// Deserialize JSON into MpesaResponse object
				ObjectMapper objectMapper = new ObjectMapper();
				MpesaResponse response = objectMapper.readValue(responseJson, MpesaResponse.class);
				var currentApp = this.appService.findAppByAppKey(appKey); // return responseJson;

				if (currentApp.isPresent()) {

					this.saveOrUpdatePayment(stk, currentApp.get(), response.getCheckoutRequestID());
					stk.setExternalCheckoutId(response.getCheckoutRequestID());
					this.rabitMqSenderService.sendMpesaCheckoutRequestId(stk);

					return new Gson().fromJson(responseJson, Map.class);
				}

			}

		} catch (Exception ex) {
			ex.printStackTrace();
			ObjectNode node = JsonNodeFactory.instance.objectNode();
			node.put("success", false);
			node.put("message", "An error ocurred");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(node);
		}

		return null;
	}

	private Payment createPaymentRequest(String appKey, String konnectTransactionId, User user) {
		var currentApp = this.appService.findAppByAppKey(appKey); // return responseJson;

		if (currentApp.isPresent()) {
			var pay = Payment.builder().app(currentApp.get())

					.konnectCheckoutId(konnectTransactionId).txtId(konnectTransactionId).user(user).isSuccessful(false)
					.verified(false).build();
			return this.paymentRepository.save(pay);

		}
		return null;
	}
	


	
	public Object validatePayment(MpesaPaymentValidationDto data) {
	    String transAmount = data.getTransAmount();
	    if (transAmount.contains(".")) {
	        transAmount = transAmount.split("\\.")[0];
	    }

	    List<String> packages = this.packageBean.packagePrices;

//	    if (packages.contains(transAmount)) {
	        ObjectNode response = JsonNodeFactory.instance.objectNode();
	        response.put("ResultCode", "0");
	        response.put("ResultDesc", "Accepted");
	        return ResponseEntity.status(HttpStatus.OK).body(response);
//	    }
	    
	    
	    

//	    ObjectNode response = JsonNodeFactory.instance.objectNode();
//	    response.put("ResultCode", "C2B00013");
//	    response.put("ResultDesc", "Rejected");
//	    return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	
	@RabbitListener(queues = "transactionStatusQueue")
	public void getTxStatusByTxId(MpesaPaymentValidationDto data) throws Exception {
		ObjectNode params = JsonNodeFactory.instance.objectNode();
		params.put("Initiator",initiator);
		params.put("SecurityCredential",this.authService.generateSecurityCredential(mpesaPassword));
		params.put("CommandID","TransactionStatusQuery");
		params.put("TransactionID",data.getTransID());
		params.put("PartyA",businessShortCode);
		params.put("IdentifierType","4");
		params.put("ResultURL","https://abf0-105-29-165-234.ngrok-free.app/konnect-wifi/payment/result");
		params.put("QueueTimeOutURL","https://abf0-105-29-165-234.ngrok-free.app/konnect-wifi/payment/result");
		params.put("Remarks","OK");
		params.put("Occasion","OK");
		
		Mono<String> responseMono = this.mpesaClient.webClient.post().uri(MpesaEndpointsConstants.TX_STATUS)
				.header("Authorization", "Bearer " + this.authService.getMpesaAccessToken())
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(params))
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
		String json =  responseMono.block();
		System.out.println("{json}"+json);
		
	}
	
	
	public Object createPaymentRequest(ToolkitPayDto req) {
		App app = null;
		if(req.getAppKey() !=null) {
			Optional<App> appOpt =  this.appRepository.findFirstByAppKeyAndAppSecret(req.getAppKey());
			if(appOpt.isEmpty()) {
				app = appOpt.get();
			}
		}
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		var payment_checkoutId =AdvancedUniqueKeyGenerator.generateUniqueKey().toUpperCase();
		var payment = Payment.builder().app(app).konnectCheckoutId(payment_checkoutId).user(user).mobileNumber(req.getMobileNumber()).build();
		this.paymentRepository.save(payment);
		return payment_checkoutId;
	}
	
	public Optional<Payment> getPaymentByMobileNumber(String mobileNumber) {
		return	this.paymentRepository.findByMobileNumber(mobileNumber);
	}

	

	public void updatePaymentWithCheckoutId(String checkoutRequestID, String requestBody) {
		this.rabitMqSenderService.updatePayment(requestBody);
		// TODO Auto-generated method stub

	}
	
	public void processMpesaStatusResult(MpesaResultDto result) {
		var res = result.getResult();
		if(res !=null && res.getResultCode() == 0 ) {
			Optional<Payment> paymentOpt = this.getPaymentByMobileNumber(res.getReferenceData().getReferenceItem().getKey());
			if(paymentOpt.isPresent()) {
				var payment = paymentOpt.get();
				this.rabitMqSenderService.requestPaymentStatus(null);
			}

		}
	}
}
