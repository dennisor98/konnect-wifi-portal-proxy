package net.sasakonnect.wifi_portal.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import net.sasakonnect.wifi_portal.beans.MpesaWebClientBean;
import net.sasakonnect.wifi_portal.constants.MpesaEndpointsConstants;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class PaymentService {
	@Autowired
	MpesaWebClientBean mpesaClient;
	
	@Value("${merchantAccount}")
	String shortCode;
	
	@Value("${mpesaUserName}")
	String password;
		
	
	@Autowired
	AuthService authService;
	
	
	public ResponseEntity<Object> mpesacallBackUrl(Object request){
		return null;
	}
	public Object stkPush() { 

		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		var timestamp =  LocalDateTime.now().format(format);
		ObjectNode req = JsonNodeFactory.instance.objectNode();
		req.put("BusinessShortCode","174379");
		req.put("Password",this.authService.getMpesaMerchantPassword(timestamp));
		req.put("Timestamp",timestamp);
		req.put("TransactionType","CustomerPayBillOnline");
		req.put("Amount","1");
		req.put("PartyA","254769156995");
		req.put("PartyB", shortCode);
		req.put("PhoneNumber", "254769156995");
		req.put("CallBackURL","https://c417-105-29-165-234.ngrok-free.app/konnect-wifi/money/callBack");
		req.put("AccountReference", "Test");
		req.put("TransactionDesc", "Test");

		
			log.error(req+"{req}");
			Mono<String> responseMono = this.mpesaClient.webClient
			        .post()
			        .uri(MpesaEndpointsConstants.STK_PUSH)
			        .header("Authorization", this.authService.getMpesaAccessToken())
			        .contentType(MediaType.APPLICATION_JSON)
			        .body(BodyInserters.fromValue(req))
			        .accept(MediaType.APPLICATION_JSON)
			        .retrieve()
			        
			        .bodyToMono(String.class);

			   log.error(responseMono+"{mono ...}");
			   String responseJson = responseMono.block();
//			responseJson.er
			if(responseJson !=null) {
				//    	   return responseJson;
				return new Gson().fromJson(responseJson,Map.class);
			}
		
		
		return null;
	}
}
