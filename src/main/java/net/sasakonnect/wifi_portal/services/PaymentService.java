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
//		{    
//			   "BusinessShortCode": "174379",    
//			   "Password": "MTc0Mzc5YmZiMjc5ZjlhYTliZGJjZjE1OGU5N2RkNzFhNDY3Y2QyZTBjODkzMDU5YjEwZjc4ZTZiNzJhZGExZWQyYzkxOTIwMTYwMjE2MTY1NjI3",    
//			   "Timestamp":"20160216165627",    
//			   "TransactionType": "CustomerPayBillOnline",    
//			   "Amount": "1",    
//			   "PartyA":"254708374149",    
//			   "PartyB":"174379",    
//			   "PhoneNumber":"254708374149",    
//			   "CallBackURL": "https://mydomain.com/pat",    
//			   "AccountReference":"Test",    
//			   "TransactionDesc":"Test"
//			}
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
       var timestamp =  LocalDateTime.now().format(format);
		Map<String,Object> req =  new HashMap<>();
		
		req.put("BusinessShortCode",174379);
		req.put("Password",this.authService.getMpesaMerchantPassword(timestamp));
		req.put("Timestamp",timestamp);
		req.put("TransactionType","CustomerPayBillOnline");
		req.put("Amount","1");
		req.put("PartyA","254769156995");
		req.put("PartyB", shortCode);
		req.put("PhoneNumber", "254769156995");
		req.put("CallBackURL","https://c417-105-29-165-234.ngrok-free.app/konnect-wifi/payment/callBack");
		req.put("AccountReference", "Test");
		req.put("TransactionDesc", "Test");
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			
			var body =  mapper.writeValueAsString(req);
			log.error(body+"{body}");
			Mono<String> responseMono = this.mpesaClient.webClient
			        .post()
			        .uri(MpesaEndpointsConstants.STK_PUSH)
			        .header("Authorization", this.authService.getMpesaAccessToken())
			        .contentType(MediaType.APPLICATION_JSON)
			        .body(BodyInserters.fromValue(body))
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
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
}
