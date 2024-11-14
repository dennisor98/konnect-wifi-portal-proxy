package net.sasakonnect.wifi_portal.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import net.sasakonnect.wifi_portal.RequestDto.StkPushDto;
import net.sasakonnect.wifi_portal.beans.MpesaWebClientBean;
import net.sasakonnect.wifi_portal.constants.MpesaEndpointsConstants;
import net.sasakonnect.wifi_portal.domain.InternetPackages;
import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.repository.InternetPackageRepository;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class PaymentService {
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
	
	public Object getTxStatusByCheckoutRequestId(String checkoutRequestId) {
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		var timestamp =  LocalDateTime.now().format(format);
		ObjectNode body = JsonNodeFactory.instance.objectNode();
		body.put("BusinessShortCode",shortCode);
		body.put("Password",this.authService.getMpesaMerchantPassword(timestamp));
		body.put("Timestamp", timestamp);
		body.put("CheckoutRequestID",checkoutRequestId);
		
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
