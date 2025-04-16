package net.sasakonnect.wifi_portal.services;

import net.sasakonnect.wifi_portal.domain.Payment;
import net.sasakonnect.wifi_portal.domain.PaymentMethod;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.sasakonnect.wifi_portal.RequestDto.AppToolKitPayDto;
import net.sasakonnect.wifi_portal.RequestDto.MerchantTransactionNotificationDto;
import net.sasakonnect.wifi_portal.RequestDto.MpesaCallBackDto;
import net.sasakonnect.wifi_portal.RequestDto.MpesaPaymentValidationDto;
import net.sasakonnect.wifi_portal.RequestDto.MpesaResultDto;
import net.sasakonnect.wifi_portal.RequestDto.PayMethodDto;
import net.sasakonnect.wifi_portal.RequestDto.PollMpesaDto;
import net.sasakonnect.wifi_portal.RequestDto.PollTxStatusDto;
import net.sasakonnect.wifi_portal.RequestDto.StkPushDto;
import net.sasakonnect.wifi_portal.RequestDto.ToolkitPayDto;
import net.sasakonnect.wifi_portal.RequestDto.UpdatePayMethodDto;
import net.sasakonnect.wifi_portal.RequestDto.sdk.MpesaResponse;
import net.sasakonnect.wifi_portal.RequestDto.sdk.PaymentRequest;
import net.sasakonnect.wifi_portal.ResponseDto.StkCallbackResponseDTO;
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
import net.sasakonnect.wifi_portal.repository.PayMethodRepository;
import net.sasakonnect.wifi_portal.repository.PaymentRepository;
import net.sasakonnect.wifi_portal.repository.UserRepository;
import reactor.core.publisher.Mono;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.http.HttpStatusCode;

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
	@Value("${mpesaCallBackUrl}")
	String mpesaCallBackUrl;
	@Value("${wifi.app.key}")
	String wifiAppKey;
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
	@Value("${api.message.url}")
	String messageUrl;
	@Value("${api.message.auth}")
	String messageAuth;
	@Autowired
	UserRepository userRepository;
	@Autowired
	RabbitMqSenderService rabbitSendService;
	@Autowired
	PayMethodRepository payMethodRepository;
	@Autowired
	LarkService larkService;
	private final RabbitTemplate rabbitTemplate;
	private final RedisTemplate<String, String> redisTemplate;
	public PaymentService(RabbitTemplate rabbitTemplate,RedisTemplate<String, String> redisTemplate) {
		this.rabbitTemplate = rabbitTemplate;
		this.redisTemplate = redisTemplate;
	}

	@Transactional
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

	@Transactional
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
	@Transactional
	public void handlePaymentRequest(String jsonPayload) {
		try {
			PaymentRequest paymentRequest = new ObjectMapper().readValue(jsonPayload, PaymentRequest.class);
			System.out.println("Received Payment Request: " + paymentRequest);

			this.createPaymentRequest(paymentRequest);

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

	

	
	
	@RabbitListener(queues = "transactionConfirmedNotificationQueue")
	@Transactional
	public void handleTransactionConfirmationNotification(MerchantTransactionNotificationDto payment) {
		String transAmount = payment.getTransAmount();
		if (transAmount.contains(".")) {
			transAmount = transAmount.split("\\.")[0];
		}
	    System.out.println(payment);
		ObjectNode resp =  JsonNodeFactory.instance.objectNode();
	     resp.put("TransType",payment.getTransType());
	     resp.put("TransID", payment.getTransId());
	     resp.put("TransAmount",transAmount);
	     resp.put("TransTime",payment.getTransTime());
	     resp.put("BusinessShortCode",payment.getBusinessShortCode());
	     resp.put("packageId",this.getPackageIdByCost(transAmount));
	     resp.put("BillRefNumber",payment.getBillRefNumber());
	     resp.put("Mobile",payment.getMobile());
	     resp.put("name",payment.getName());
	     resp.put("userId",payment.getUserId());
	     resp.put("KonnectTransID",payment.getKonnectTransId());
	     resp.put("ResultCode","0");
	     resp.put("staMac",payment.getDeviceMac());
	     resp.put("actNow",payment.getActNow() !=null && payment.getActNow()?"true":"false");
	     resp.put("initiator",payment.getPlatform());
	     log.error(payment+"{}");
	     log.error(resp+"{body}");
	     
	     threadExceutorBean.addTask(new Runnable() {

			@Override
			public void run() {
				Mono<Void> respMono = webClient.webClient.post().uri(payment.getApp().getCallbackUrl())
						.contentType(MediaType.APPLICATION_JSON)
						.body(BodyInserters.fromValue(resp.toPrettyString()))
						.accept(MediaType.APPLICATION_JSON)
						.exchangeToMono(clientResponse -> {
                            HttpStatus statusCode = (HttpStatus) clientResponse.statusCode();
                            if(statusCode.value() !=200) {
                            	rabbitSendService.addToFailedPaymentNotificationQueue(payment,"0");
                            }
                            return clientResponse.bodyToMono(Void.class);
                        });
				respMono.block();	
				
			     notifyKonnectWater(payment);

			}
	    	 
	     });
	
	}
	
	@RabbitListener(queues = "transactionCallBackNotificationQueue")
	@Transactional
	public void handleTransactionConfirmationCallBackNotification(MerchantTransactionNotificationDto payment,Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
		try {
			log.error("notifier called.."+payment);
			String transAmount = payment.getTransAmount();
			if (transAmount.contains(".")) {
				transAmount = transAmount.split("\\.")[0];
			}
		    System.out.println(payment);
			ObjectNode resp =  JsonNodeFactory.instance.objectNode();
		     resp.put("TransType",payment.getTransType());
		     resp.put("TransID", payment.getTransId());
		     resp.put("TransAmount",transAmount);
		     resp.put("TransTime",payment.getTransTime());
		     resp.put("BusinessShortCode",payment.getBusinessShortCode());
		     resp.put("packageId",this.getPackageIdByCost(transAmount));
		     resp.put("BillRefNumber",payment.getBillRefNumber());
		     resp.put("Mobile",payment.getMobile());
		     resp.put("name",payment.getName());
		     resp.put("userId",payment.getUserId());
		     resp.put("KonnectTransID",payment.getKonnectTransId());
		     resp.put("ResultCode","0");
		     resp.put("staMac",payment.getDeviceMac());
		     resp.put("initiator",payment.getPlatform());
		     resp.put("actNow",payment.getActNow() !=null && payment.getActNow()?"true":"false");
		     log.error(payment+"{}");
		     log.error(resp+"{body}");
		     threadExceutorBean.addTask(new Runnable()  {
				 @Override
				public void run(){
					 notifyKonnectWater(payment);
				 }
			 });
	        
		     threadExceutorBean.addTask(new Runnable() {

		    	 @Override
		    	 public void run() {
		    		 log.error("Executing task...");
		    		 try {
		    			 Mono<?> respMono = webClient.webClient.post()
		    					 .uri(payment.getApp().getCallbackUrl())
		    					 .contentType(MediaType.APPLICATION_JSON)
		    					 .body(BodyInserters.fromValue(resp.toPrettyString()))
		    					 .accept(MediaType.APPLICATION_JSON)
		    					 .exchangeToMono(clientResponse -> {
		    						 HttpStatusCode statusCode = clientResponse.statusCode();

		    						 if (statusCode.value() != 200) {
		    							 log.error("Request failed with status: {}", statusCode);
		    							 rabbitSendService.addToFailedPaymentNotificationQueue(payment, "0");
		    						 }

		    						 return clientResponse.bodyToMono(String.class);
		    					 })
		    					 .doOnError(error -> {
		    						 log.error("Error occurred while making request: {}", error.getMessage(), error);
		    						 rabbitSendService.addToFailedPaymentNotificationQueue(payment, "0");
		    					 });

		    			 var response = respMono.block();
		    			 log.error("Response: {}", response);
		    		 } catch (Exception e) {
		    			 log.error("Unexpected error during request execution: {}", e.getMessage(), e);
		    			 rabbitSendService.addToFailedPaymentNotificationQueue(payment, "0");
		    		 }
		    	 }});
		     channel.basicAck(deliveryTag, false);
		}catch(Exception ex) {
			try {
				channel.basicReject(deliveryTag, false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	     

	}

	@RabbitListener(queues = "failedPaymentNotificationQueue0")
	@Transactional
	public void handleFailedPaymentNotification0(MerchantTransactionNotificationDto payment,Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
		threadExceutorBean.addTask(new Runnable() {

			@Override
			public void run() {
				ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
				executor.schedule(() -> {
					try {
						String transAmount = payment.getTransAmount();
						ObjectNode resp =  JsonNodeFactory.instance.objectNode();
						resp.put("TransType",payment.getTransType());
						resp.put("TransID", payment.getTransId());
						resp.put("TransAmount",transAmount);
						resp.put("TransTime",payment.getTransTime());
						resp.put("BusinessShortCode",payment.getBusinessShortCode());
						resp.put("packageId",getPackageIdByCost(transAmount));
						resp.put("BillRefNumber",payment.getBillRefNumber());
						resp.put("Mobile",payment.getMobile());
						resp.put("name",payment.getName());
						resp.put("userId",payment.getUserId());
						resp.put("KonnectTransID",payment.getKonnectTransId());
						resp.put("ResultCode","0");
						resp.put("staMac",payment.getDeviceMac());
						resp.put("initiator",payment.getPlatform());
						resp.put("actNow",payment.getActNow() !=null && payment.getActNow()?"true":"false");
						Mono<Void> responseMono = webClient.webClient.post()
	                            .uri(payment.getApp().getCallbackUrl())
	                            .contentType(MediaType.APPLICATION_JSON)
	                            .body(BodyInserters.fromValue(resp.toPrettyString()))
	                            .accept(MediaType.APPLICATION_JSON)
	                            .exchangeToMono(clientResponse -> {
	                                HttpStatus statusCode = (HttpStatus) clientResponse.statusCode();
	                                if(statusCode.value() !=200) {
	                                	rabbitSendService.addToFailedPaymentNotificationQueue(payment,"1");
	                                }
	                                return clientResponse.bodyToMono(Void.class);
	                            });
						responseMono.block();	
						//acknowledge and don't requeue
						channel.basicAck(deliveryTag, false);
					} catch (Exception e) {
						try {
							channel.basicReject(deliveryTag, false);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						e.printStackTrace();
					} finally {
						executor.shutdown();
					}
				}, 5, TimeUnit.SECONDS);
			}

		});
	}
	
	@RabbitListener(queues = "failedPaymentNotificationQueue1")
	@Transactional
	public void handleFailedPaymentNotification1(MerchantTransactionNotificationDto payment,Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
		threadExceutorBean.addTask(new Runnable() {
			@Override
			public void run() {
				ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
				executor.schedule(() -> {
					try {
						String transAmount = payment.getTransAmount();
						ObjectNode resp =  JsonNodeFactory.instance.objectNode();
						resp.put("TransType",payment.getTransType());
						resp.put("TransID", payment.getTransId());
						resp.put("TransAmount",transAmount);
						resp.put("TransTime",payment.getTransTime());
						resp.put("BusinessShortCode",payment.getBusinessShortCode());
						resp.put("packageId",getPackageIdByCost(transAmount));
						resp.put("BillRefNumber",payment.getBillRefNumber());
						resp.put("Mobile",payment.getMobile());
						resp.put("name",payment.getName());
						resp.put("userId",payment.getUserId());
						resp.put("KonnectTransID",payment.getKonnectTransId());
						resp.put("ResultCode","0");
						resp.put("staMac",payment.getDeviceMac());
						resp.put("initiator",payment.getPlatform());
						resp.put("actNow",payment.getActNow() !=null && payment.getActNow()?"true":"false");
						Mono<Object> respMono = webClient.webClient.post().uri(payment.getApp().getCallbackUrl())
								.contentType(MediaType.APPLICATION_JSON)
								.body(BodyInserters.fromValue(resp.toPrettyString()))
								.accept(MediaType.APPLICATION_JSON)
								.exchangeToMono(clientResponse -> {
	                                HttpStatus statusCode = (HttpStatus) clientResponse.statusCode();
	                                if(statusCode.value() !=200) {
	                                	rabbitSendService.addToFailedPaymentNotificationQueue(payment,"2");
	                                }
	                                return clientResponse.bodyToMono(Void.class);
	                            });
						respMono.block();
						channel.basicAck(deliveryTag, false);
					} catch (Exception e) {
						e.printStackTrace();
						try {
							channel.basicReject(deliveryTag, false);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} finally {
						executor.shutdown();
					}
				}, 10, TimeUnit.SECONDS);
			}

		});
	}
	
	@RabbitListener(queues = "failedPaymentNotificationQueue2")
	@Transactional
	public void handleFailedPaymentNotification2(MerchantTransactionNotificationDto payment, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
		threadExceutorBean.addTask(new Runnable() {

			@Override
			public void run() {
				ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
				executor.schedule(() -> {
					try {
						String transAmount = payment.getTransAmount();
						ObjectNode resp =  JsonNodeFactory.instance.objectNode();
						resp.put("TransType",payment.getTransType());
						resp.put("TransID", payment.getTransId());
						resp.put("TransAmount",transAmount);
						resp.put("TransTime",payment.getTransTime());
						resp.put("BusinessShortCode",payment.getBusinessShortCode());
						resp.put("packageId",getPackageIdByCost(transAmount));
						resp.put("BillRefNumber",payment.getBillRefNumber());
						resp.put("Mobile",payment.getMobile());
						resp.put("name",payment.getName());
						resp.put("userId",payment.getUserId());
						resp.put("KonnectTransID",payment.getKonnectTransId());
						resp.put("ResultCode","0");
						resp.put("staMac",payment.getDeviceMac());
						resp.put("initiator",payment.getPlatform());
						resp.put("actNow",payment.getActNow() !=null && payment.getActNow()?"true":"false");
						Mono<Object> respMono = webClient.webClient.post().uri(payment.getApp().getCallbackUrl())
								.contentType(MediaType.APPLICATION_JSON)
								.body(BodyInserters.fromValue(resp.toPrettyString()))
								.accept(MediaType.APPLICATION_JSON).retrieve()
								.bodyToMono(Object.class);
						respMono.block();	
						channel.basicAck(deliveryTag,false);
					} catch (Exception e) {
						try {
							
							//reject and don't requeue
							channel.basicReject(deliveryTag,false);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						e.printStackTrace();
					} finally {
						executor.shutdown();
					}
				}, 15, TimeUnit.SECONDS);
			}

		});
	}



	@RabbitListener(queues = "checkOutIdConfirmationQueue")
	@Transactional
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


	public Object stkPush(StkPushDto stk) {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		var phone = stk.getPhone();
		String mobile = null;
		String appKey = "1f1a7632a32665039b1e98952cafd3ac54b288f170177c4e5a2483c143ceb8f0";
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
          Optional<InternetPackages> packageOpt = this.internetPackageRepository.findByForeignPackageId(stk.getSubscriptionPlanId());
          if(packageOpt.isEmpty()) {
        	  ObjectNode res =  JsonNodeFactory.instance.objectNode();
        	  res.put("success", false);
        	  res.put("message", "Invalid subscriptionPlanId");
        	  
        	  return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
          }
          var pkg = packageOpt.get();
			var payReq = PaymentRequest.builder().phoneNumber("254"+mobile).app(app).appKey(appKey).staMac(stk.getStaMac()).amount(pkg.getCost()).user(user).build();
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
	
	public String getPackageIdByCost(String cost) {
		Optional<InternetPackages> packageOpt = this.internetPackageRepository.findByCost(Integer.valueOf(cost));
			if(packageOpt.isPresent()) {
			var iPackage = packageOpt.get();
			return iPackage.getForeignPackageId();
		}
		return null;
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

	private Payment createPaymentRequest(PaymentRequest payment) {
		var currentApp = this.appService.findAppByAppKey(payment.getAppKey()); // return responseJson;

		if (currentApp.isPresent()) {
			var pay = Payment.builder().app(currentApp.get())

					.konnectCheckoutId(payment.getKonnectCheckoutID()).txtId(payment.getKonnectCheckoutID()).deviceMac(payment.getStaMac()).user(payment.getUser()).idUser(payment.getUser() !=null ? payment.getUser().getUserId() : null).amount(String.valueOf(payment.getAmount())).isSuccessful(false)
					.verified(false).build();
			return this.paymentRepository.save(pay);

		}
//		this.redisService.addTransactionIten(konnectTransactionId);
		return null;
	}




	public Object validatePayment(MpesaPaymentValidationDto data) {
		String transAmount = data.getTransAmount();
		if (transAmount.contains(".")) {
			transAmount = transAmount.split("\\.")[0];
		}

		List<String> packages = this.packageBean.packagePrices;

		if (packages.contains(transAmount)) {
		ObjectNode response = JsonNodeFactory.instance.objectNode();
		response.put("ResultCode", "0");
		response.put("ResultDesc", "Accepted");
		return ResponseEntity.status(HttpStatus.OK).body(response);
		}
		ObjectNode response = JsonNodeFactory.instance.objectNode();
		response.put("ResultCode", "C2B00013");
		response.put("ResultDesc", "Rejected");
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}


	@RabbitListener(queues = "transactionStatusQueue")
	public void getTxStatusByTxId(MpesaPaymentValidationDto data,Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws Exception {
		try {
			ObjectNode params = JsonNodeFactory.instance.objectNode();
			params.put("Initiator",initiator);
			params.put("SecurityCredential",this.authService.generateSecurityCredential(mpesaPassword));
			params.put("CommandID","TransactionStatusQuery");
			params.put("TransactionID",data.getTransID());
			params.put("PartyA",businessShortCode);
			params.put("IdentifierType","4");
			params.put("ResultURL","https://mfood.sasakonnect.net/konnect-wifi/payment/result");
			params.put("QueueTimeOutURL","https://mfood.sasakonnect.net/konnect-wifi/payment/result");
			params.put("Remarks","OK");
			params.put("Occasion","OK");

			Mono<String> responseMono = this.mpesaClient.webClient.post().uri(MpesaEndpointsConstants.TX_STATUS)
					.header("Authorization", "Bearer " + this.authService.getMpesaAccessToken())
					.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(params))
					.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
			String json =  responseMono.block();
			System.out.println("{json}"+json);
			
			 channel.basicAck(deliveryTag, false);
		}catch(Exception ex) {
			channel.basicReject(deliveryTag,true);
		}

	}

	
	public Object createMerchantPaymentRequest(ToolkitPayDto req) {
		log.error("payload",req);
		App app = null;
		if(req.getAppKey() !=null) {
			Optional<App> appOpt =  this.appRepository.findFirstByAppKeyAndAppSecret(req.getAppKey());
			if(appOpt.isPresent()) {
				app = appOpt.get();
			}
		}
		Optional<User> userOpt = this.userRepository.findByUserId(req.getUserId());
		String phoneStr =  req.getMobileNumber();
		if(phoneStr.length() < 9) {
			Map<String,Object> map = new HashMap<>();
			map.put("success",false);
			map.put("message","Phone number must be at least 9 digits");
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
		}
		String phone = phoneStr.substring(phoneStr.length()-9);
		String mobile = "254"+phone;
		User user = userOpt.isPresent() ? userOpt.get() : null;
		var payment_checkoutId =AdvancedUniqueKeyGenerator.generateUniqueKey().toUpperCase();
		log.error("payload",req.getAuthAttempt());
		var payment = Payment.builder().app(app).konnectCheckoutId(payment_checkoutId).idUser(req.getUserId()).user(user).isSuccessful(false).verified(false).source(app.getName()).activateNow(req.getActNow())
				.amount(String.valueOf(req.getAmount())).mobileNumber(mobile).deviceMac(req.getAuthAttempt() !=null ? req.getAuthAttempt().getStaMac() : null).build();
		this.paymentRepository.save(payment);
		return payment_checkoutId;
	}

	public Object getTransactionStatus(String transId) throws Exception {
		ObjectNode params = JsonNodeFactory.instance.objectNode();
		params.put("Initiator",initiator);
		params.put("SecurityCredential",this.authService.generateSecurityCredential(mpesaPassword));
		params.put("CommandID","TransactionStatusQuery");
		params.put("TransactionID",transId);
		params.put("PartyA",businessShortCode);
		params.put("IdentifierType","4");
		params.put("ResultURL","https://83d1-105-29-165-233.ngrok-free.app/konnect-wifi-dev/payment/result");
		params.put("QueueTimeOutURL","https://83d1-105-29-165-233.ngrok-free.app/konnect-wifi-dev/payment/result");
		params.put("Remarks","OK");
		params.put("Occasion","OK");

		Mono<String> responseMono = this.mpesaClient.webClient.post().uri(MpesaEndpointsConstants.TX_STATUS)
				.header("Authorization", "Bearer " + this.authService.getMpesaAccessToken())
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(params))
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
		String json =  responseMono.block();
		System.out.println("{json}"+json);
		
		return ResponseEntity.status(HttpStatus.OK).body(json);	
}

	public Object createPaymentRequest(AppToolKitPayDto req) {
		App app = null;		
		Optional<App> appOpt =  this.appRepository.findFirstByAppKeyAndAppSecret(wifiAppKey);
		if(appOpt.isPresent()) {
			app = appOpt.get();
		}
		String mobile = "254"+req.getMobileNumber().substring(req.getMobileNumber().length() -9);
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		var payment_checkoutId =AdvancedUniqueKeyGenerator.generateUniqueKey().toUpperCase();
		var payment = Payment.builder()
				.app(app)
				.deviceMac(req.getStaMac())
				.idUser(user.getUserId())
				.konnectCheckoutId(payment_checkoutId)
				.user(user).mobileNumber(mobile)
				.isSuccessful(false).verified(false)
				.source("super-app").activateNow(req.getActNow())
				.build();
		
		this.paymentRepository.save(payment);
		return payment_checkoutId;
	}

	public Optional<Payment> getPaymentByMobileNumber(String mobileNumber) {
		return	this.paymentRepository.findxByMobileNumber(mobileNumber.trim());
	}



	public void updatePaymentWithCheckoutId(String checkoutRequestID, String requestBody) {
		this.rabitMqSenderService.updatePayment(requestBody);
	}
    
	public String getValueByKey(String key, StkCallbackResponseDTO dto) {
	    if (dto == null 
	        || dto.getBody() == null 
	        || dto.getBody().getStkCallback() == null 
	        || dto.getBody().getStkCallback().getCallbackMetadata() == null 
	        || dto.getBody().getStkCallback().getCallbackMetadata().getItem() == null) {
	        return null;
	    }

	    List<StkCallbackResponseDTO.Item> items = dto.getBody().getStkCallback().getCallbackMetadata().getItem();

	    return items.stream()
	            .filter(item -> key.equalsIgnoreCase(item.getName())) 
	            .map(item -> item.getValue().toString()) 
	            .findFirst()
	            .orElse(null); 
	}

	
	public  String getValueByKey(String key,MpesaResultDto dto) {
		if (dto == null || dto.getResult() == null || dto.getResult().getResultParameters() == null) {
			return null; 
		}

		List<MpesaResultDto.ResultParameterDto> parameters = dto.getResult().getResultParameters().getResultParameter();

		if (parameters == null) {
			return null; 
		}

		return parameters.stream()
				.filter(param -> key.equals(param.getKey()))
				.map(MpesaResultDto.ResultParameterDto::getValue)
				.findFirst()
				.orElse(null); // Return null if not found
	}
	
	
	
	@Transactional
	public void processMpesaStatusResult(MpesaResultDto result) {
		var res = result.getResult();
		if(res !=null && res.getResultCode() == 0 ) {
			 //process transaction from redis
			String key = "tx:"+getValueByKey("ReceiptNo", result);
			String str = this.redisTemplate.opsForValue().get(key);
			if(str != null) {
				return;
			}
			
			this.redisTemplate.opsForValue().set("tx:"+getValueByKey("ReceiptNo", result),getValueByKey("ReceiptNo", result),Duration.ofHours(24));
			
			String mobile = this.getValueByKey("DebitPartyName", result).split("-")[0].trim();
			String sanitizedMobile = mobile.length() >= 9 ? "254"+mobile.substring(mobile.length() - 9) : "254"+mobile;
			Optional<Payment> paymentOpt = this.getPaymentByMobileNumber(sanitizedMobile);
			Optional<Payment> payOpt =  paymentRepository.findByTxtId(getValueByKey("ReceiptNo", result));
           
			//ensure there is a payment req && the transId processed is unique
			if(paymentOpt.isPresent() && payOpt.isEmpty()) {				
						var payment = paymentOpt.get();
						this.redisTemplate.opsForValue().set("ktx:"+payment.getKonnectCheckoutId(),payment.getKonnectCheckoutId(),Duration.ofHours(24));
						payment.setVerified(true);
						payment.setIsSuccessful(true);
						payment.setPaymentPayload(String.valueOf(result));
						payment.setPaymentVerificationPayload(String.valueOf(result));
						payment.setAmount(getValueByKey("Amount", result));
						payment.setTxtId(getValueByKey("ReceiptNo", result));
						paymentRepository.save(payment);
						log.error("{payment}"+payment);
						var merchantNotification = MerchantTransactionNotificationDto.builder().app(payment.getApp()).billRefNumber(getValueByKey("ReceiptNo",result)).actNow(payment.getActivateNow())
								.businessShortCode(getValueByKey("CreditPartyName", result).split("-")[0]).mobile(getValueByKey("DebitPartyName", result).split("-")[0]).platform(payment.getSource())
								.konnectTransId(payment.getKonnectCheckoutId()).name(getValueByKey("DebitPartyName", result).split("-")[1]).transAmount(getValueByKey("Amount", result))
								.app(payment.getApp()).deviceMac(payment.getDeviceMac()).transId(getValueByKey("ReceiptNo", result)).transTime(getValueByKey("InitiatedTime", result)).userId(payment.getIdUser()).transType(getValueByKey("ReasonType", result))
								.build();
						log.error(merchantNotification+"{}");
						rabitMqSenderService.sendTransactionNotificationToMerchant(merchantNotification);
				
			}else {
				this.threadExceutorBean.addTask(new Runnable() {
                  
					@Override
					public void run() {
						try {
							if(payOpt.isEmpty()) {
								var payment = Payment.builder().amount(getValueByKey("Amount",result).toString())
										.mobileNumber(sanitizedMobile).isSuccessful(true).verified(false)
										.txtId(getValueByKey("ReceiptNo",result))
										.konnectCheckoutId(AdvancedUniqueKeyGenerator.generateUniqueKey().toUpperCase())
										.paymentVerificationPayload(result.toString())
										.paymentPayload(result.toString())
										.build();
								paymentRepository.save(payment);
								paymentRepository.flush();
								log.error("transaction saved");
								
							String larkMessage = 
									"<at id=all></at> \n"+
									"**Phone Number**: "+getValueByKey("DebitPartyName", result).split("-")[0]+" \n"
									+"**Name**: "+getValueByKey("DebitPartyName", result).split("-")[1]+" \n"
									+"**InitiatedTime**: "+convertDateToHumanReadableString(getValueByKey("InitiatedTime", result))+" \n"
									+"**FinalisedTime**: "+convertDateToHumanReadableString(getValueByKey("FinalisedTime",result))+"\n"
									+ "**ReceiptNo**: "+getValueByKey("ReceiptNo", result)+" \n"
									+"**Amount**: "+getValueByKey("Amount", result)+"\n"
									+"**Reason** :Payment unscheduled";

							larkService.sendPaymentNotification(larkMessage);
							}
							
							log.error("payment exists");

						}catch(Exception ex) {
							ex.printStackTrace();
						}
						
					}

				});	
			}
			
		}
	}
	
	public String convertDateToHumanReadableString(String timestamp) {

        // Define the input format
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        // Parse the timestamp to LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(timestamp, inputFormatter);

        // Define the output format
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm:ss a");

        // Convert to human-readable format
        String humanReadable = dateTime.format(outputFormatter);
        
        return humanReadable;

    }
	
	
	public void processMpesaCallBack(StkCallbackResponseDTO paymentCallBack) {
		var stkCall = paymentCallBack.getBody().getStkCallback();
		Optional<Payment> paymentOpt = this.paymentRepository.findByTxtIdIgnoreCase(stkCall.getCheckoutRequestID());
		log.error("{payment} "+paymentOpt);
		if(paymentOpt.isPresent()) {
			var payment = paymentOpt.get();
			var merchantNotification = MerchantTransactionNotificationDto.builder().app(payment.getApp()).billRefNumber(getValueByKey("MpesaReceiptNumber",paymentCallBack))
					.businessShortCode(payment.getApp().getBusinessShortCode()).mobile(this.getValueByKey("PhoneNumber", paymentCallBack)).platform("super-app")
					.konnectTransId(payment.getKonnectCheckoutId()).name(null).transAmount(this.getValueByKey("Amount", paymentCallBack))
					.deviceMac(payment.getDeviceMac()).transId(this.getValueByKey("MpesaReceiptNumber", paymentCallBack)).transTime(this.getValueByKey("TransactionDate", paymentCallBack)).userId(payment.getIdUser()).transType("Merchant Online Pay")
					.build();
			this.rabitMqSenderService.sendTransactionNotificationCallBackToMerchant(merchantNotification);
		}

	}
	
	
	
	public Object getPaymentStatusByTxId(PollTxStatusDto req) {
		String key = "ktx:"+req.getTxId();
		
		String valStr =  this.redisTemplate.opsForValue().get(key);
		if(valStr == null) {
			Map<String,Object> res = new HashMap<>();
			res.put("success",true);
			res.put("isSuccessful",false);
			res.put("transactionCode",null);
			res.put("amount",null);
			res.put("mobileNumber",null);
			res.put("userId",null);
			
			Map<String,Object> payload  = new HashMap<>();
			payload.put("result",res);
			
			return ResponseEntity.status(HttpStatus.OK).body(payload);
		}
		Optional<Payment> paymentOpt =  this.paymentRepository.findByKonnectCheckoutId(req.getTxId());
		ObjectNode res  = JsonNodeFactory.instance.objectNode();
		Map<String,Object> payload  = new HashMap<>();
		if(paymentOpt.isEmpty()) {
			res.put("success",false);
			res.put("message","Invalid txId");
			payload.put("result", res);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(payload);
		}

		var payment =  paymentOpt.get();

		res.put("success",true);
		res.put("isSuccessful",payment.getIsSuccessful());
		res.put("transactionCode",payment.getTxtId());
		res.put("amount",payment.getAmount());
		res.put("mobileNumber",payment.getMobileNumber());
		res.put("userId", payment.getIdUser());
		payload.put("result",res);
		return ResponseEntity.status(HttpStatus.OK).body(payload);
	}
	
	public Object getTxStatusResult() {
		//process the transaction result
		return null;
	}
	
	
	
//	@RabbitListener()
	public Object  getPaymentDetailsByMpesaCode(String mpesaCode) throws Exception {
		ObjectNode params = JsonNodeFactory.instance.objectNode();
		params.put("Initiator",initiator);
		params.put("SecurityCredential",this.authService.generateSecurityCredential(mpesaPassword));
		params.put("CommandID","TransactionStatusQuery");
		params.put("TransactionID",mpesaCode);
		params.put("PartyA",businessShortCode);
		params.put("IdentifierType","4");
		params.put("ResultURL","https://mfood.sasakonnect.net/konnect-wifi/payment/result");
		params.put("QueueTimeOutURL","https://mfood.sasakonnect.net/konnect-wifi/payment/result");
		params.put("Remarks","OK");
		params.put("Occasion","OK");

		Mono<String> responseMono = this.mpesaClient.webClient.post().uri(MpesaEndpointsConstants.TX_STATUS)
				.header("Authorization", "Bearer " + this.authService.getMpesaAccessToken())
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(params))
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
		String json =  responseMono.block();
		System.out.println("{json}"+json);
		return null;
	}
	
	
	public void notifyKonnectWater(MerchantTransactionNotificationDto notification) {
		String transAmount = notification.getTransAmount();
		if (transAmount.contains(".")) {
			transAmount = transAmount.split("\\.")[0];
		}
	    Map<String, Object> map = new HashMap<>();
	    map.put("phoneNumber", notification.getMobile());
	    map.put("fullName", notification.getName());
	    map.put("amount", notification.getTransAmount());
	    map.put("packageId",transAmount);
	    map.put("completedAt", notification.getTransTime());
	    map.put("transID", notification.getTransId());

	    String url = "https://gw.sasakonnect.net/konnect-water/api/v1/pkgpurchase-callback";

	    log.error("Sending request to URL: {}", url);
	    log.error("Request Payload: {}", map);
	    try {
	        Mono<String> responseMono = this.webClient.webClient.post()
	                .uri(url)
	                .contentType(MediaType.APPLICATION_JSON)
	                .bodyValue(map)
	                .accept(MediaType.APPLICATION_JSON)
	                .retrieve()
	                .bodyToMono(String.class)
	                .doOnSuccess(response -> log.info("Response: {}", response))
	                .doOnError(error -> log.error("Request failed", error));

	        String jsonResponse = responseMono.block();
	        log.info("Final Response: {}", jsonResponse);
	    } catch (WebClientResponseException ex) {
	        log.error("HTTP Status: {} | Response: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
	    } catch (Exception ex) {
	        log.error("Unexpected error", ex);
	    }

	}
	
	
	public Object getPayments(Pageable pageable,String filter) {
		log.error("filter "+filter);
		Page<Payment> paymentList;
		if(filter.equalsIgnoreCase("all")) {
			log.error("all");	
			paymentList = this.paymentRepository.findAll(pageable);
		}
		else if(filter !=null && filter.equalsIgnoreCase("verified")) {
			log.error("verified");	
			paymentList = this.paymentRepository.findByVerifiedAndIsSuccessful(true,true,pageable);
		}else {
			log.error("unverified");	
			paymentList = this.paymentRepository.findByVerifiedAndIsSuccessful(false,true,pageable);
		}
			
		
		
	    Map<String, Object> pageInfo = new HashMap<>();
	    pageInfo.put("totalPages", paymentList.getTotalPages());
	    pageInfo.put("totalElements", paymentList.getTotalElements());
	    pageInfo.put("currentPage", paymentList.getNumber());
	    pageInfo.put("pageSize", paymentList.getSize());
	    pageInfo.put("hasPreviousPage", paymentList.hasPrevious());
	    pageInfo.put("hasNextPage", paymentList.hasNext());
	    pageInfo.put("previousPage", paymentList.hasPrevious() ? paymentList.previousPageable().getPageNumber() : null);
	    pageInfo.put("nextPage", paymentList.hasNext() ? paymentList.nextPageable().getPageNumber() : null);

	    List<Map<String, Object>> payments = paymentList.stream()
	        .map(p -> {
	            Map<String, Object> paymentMap = new HashMap<>();
	            paymentMap.put("createdAt", p.getCreatedAt());
	            paymentMap.put("txId", p.getTxtId());
	            paymentMap.put("amount", p.getAmount());
	            paymentMap.put("initiator", p.getUser() != null 
	                    ? p.getUser().getFirstname() + " " + p.getUser().getLastname() 
	                    : null);
	            paymentMap.put("completed", p.getIsSuccessful());
	            paymentMap.put("verified", p.getVerified());
	            paymentMap.put("source",p.getApp() !=null ? p.getApp().getName() : null);
	            paymentMap.put("phone", p.getMobileNumber());
	            return paymentMap;
	        })
	        .collect(Collectors.toList());

	    // Build the response payload
	    Map<String, Object> payload = new HashMap<>();
	    payload.put("success", true);
	    payload.put("message", "Request complete");
	    payload.put("payments", payments);
	    payload.put("pageInfo", pageInfo);

	    // Wrap the payload in the final response map
	    Map<String, Object> res = new HashMap<>();
	    res.put("payload", payload);

	    return ResponseEntity.status(HttpStatus.OK).body(res);
	}
	
	public Object searchPayment(String searchTerm,Pageable pageable) {
		Page<Payment> paymentPage = this.paymentRepository.searchTx(searchTerm, pageable);
	    Map<String, Object> pageInfo = new HashMap<>();
	    pageInfo.put("totalPages", paymentPage.getTotalPages());
	    pageInfo.put("totalElements", paymentPage.getTotalElements());
	    pageInfo.put("currentPage", paymentPage.getNumber());
	    pageInfo.put("pageSize", paymentPage.getSize());
	    pageInfo.put("hasPreviousPage", paymentPage.hasPrevious());
	    pageInfo.put("hasNextPage", paymentPage.hasNext());
	    pageInfo.put("previousPage", paymentPage.hasPrevious() ? paymentPage.previousPageable().getPageNumber() : null);
	    pageInfo.put("nextPage", paymentPage.hasNext() ? paymentPage.nextPageable().getPageNumber() : null);

	    List<Map<String, Object>> payments = paymentPage.stream()
	        .map(p -> {
	            Map<String, Object> paymentMap = new HashMap<>();
	            paymentMap.put("createdAt", p.getCreatedAt());
	            paymentMap.put("txId", p.getTxtId() !=null ? p.getTxtId() : p.getKonnectCheckoutId());
	            
	            paymentMap.put("amount", p.getAmount());
	            paymentMap.put("initiator", p.getUser() != null 
	                    ? p.getUser().getFirstname() + " " + p.getUser().getLastname() 
	                    : null);
	            paymentMap.put("completed", p.getIsSuccessful());
	            paymentMap.put("verified", p.getVerified());
	            paymentMap.put("source",p.getApp() !=null ? p.getApp().getName() : null);
	            paymentMap.put("phone", p.getMobileNumber());
	            return paymentMap;
	        })
	        .collect(Collectors.toList());

	    Map<String, Object> payload = new HashMap<>();
	    payload.put("success", true);
	    payload.put("message", "Request complete");
	    payload.put("payments", payments);
	    payload.put("pageInfo", pageInfo);

	    Map<String, Object> res = new HashMap<>();
	    res.put("payload", payload);

	    return ResponseEntity.status(HttpStatus.OK).body(res);
		
	}
	
	
	public Object createPaymentMethod(PayMethodDto payDto) {
		User user  = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<PaymentMethod> payOpt = this.payMethodRepository.findByName(payDto.getName());
		if(payOpt.isPresent()) {
			ObjectNode res = JsonNodeFactory.instance.objectNode();
			res.put("success",false);
			res.put("message","Payment option already exist");
			
			return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
			
		}
		
	var payBuild =	PaymentMethod.builder()
			.description(payDto.getDescription())
			.isActive(payDto.getIsActive())
			.name(payDto.getName())
			.url(payDto.getApiUrl())
			.user(user)
			.build();
	
	try {
		this.payMethodRepository.save(payBuild);
		ObjectNode res = JsonNodeFactory.instance.objectNode();
		res.put("success",true);
		res.put("message","Payment option created");
		
		return ResponseEntity.status(HttpStatus.OK).body(res);
		
	}catch(Exception ex) {
		ex.printStackTrace();
		ObjectNode res = JsonNodeFactory.instance.objectNode();
		res.put("success",false);
		res.put("message","Error while processing request");
		
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
	}
		
	}
	
	public Object updatePayMethod(UpdatePayMethodDto payDto) {
		User user  = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<PaymentMethod> payOpt =  this.payMethodRepository.findById(payDto.getId());
		if(payOpt.isEmpty()) {
			ObjectNode res = JsonNodeFactory.instance.objectNode();
			res.put("success",false);
			res.put("message","Invalid id");
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
		}
		var payMethod =  payOpt.get();
		payMethod.setDescription(payDto.getDescription());
		payMethod.setIsActive(payDto.getIsActive());
		payMethod.setName(payDto.getName());
		payMethod.setUrl(payDto.getApiUrl());
		payMethod.setUser(user);
		
		try {
			ObjectNode res = JsonNodeFactory.instance.objectNode();
			res.put("success",true);
			res.put("message","Payment method edited!");
			
			return ResponseEntity.status(HttpStatus.OK).body(res);
		}catch(Exception ex) {
			ex.printStackTrace();
			ObjectNode res = JsonNodeFactory.instance.objectNode();
			res.put("success",false);
			res.put("message","Error while processing request");
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
		}
	}
	
 public Object deletePaymentMethod(String payOptionId) {
	 Optional<PaymentMethod> payOpt =  this.payMethodRepository.findById(payOptionId);
		if(payOpt.isEmpty()) {
			ObjectNode res = JsonNodeFactory.instance.objectNode();
			res.put("success",false);
			res.put("message","Invalid id");
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
		}
		var payMethod = payOpt.get();
	try {
		this.payMethodRepository.delete(payMethod);
		ObjectNode res = JsonNodeFactory.instance.objectNode();
		res.put("success",true);
		res.put("message","Payment method deleted!!");
		
		return ResponseEntity.status(HttpStatus.OK).body(res);
	}catch(Exception ex) {
		ex.printStackTrace();
		ObjectNode res = JsonNodeFactory.instance.objectNode();
		res.put("success",false);
		res.put("message","Error while processing request");
		
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
	}
 }
 
 
 public Object getPaymentMethods() {
	 List<PaymentMethod> paymethodsList = this.payMethodRepository.findAll();
	 var payMethods =  paymethodsList.stream()
			 .map(p->{
				Map<String,Object> map = new HashMap<>();
				map.put("id",p.getId());
				map.put("name",p.getName());
				map.put("description", p.getDescription());
				map.put("isActive", p.getIsActive());
				
				return map;
				}).collect(Collectors.toList());
	 
	 Map<String,Object> res = new HashMap<>();
	 res.put("sucess",true);
	 res.put("message","Request complete");
	 res.put("payOptions",payMethods);
	 
	 return ResponseEntity.status(HttpStatus.OK).body(res);
 }
 
 


}
