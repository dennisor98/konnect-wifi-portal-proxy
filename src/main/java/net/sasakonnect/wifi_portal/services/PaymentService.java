package net.sasakonnect.wifi_portal.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import net.sasakonnect.wifi_portal.beans.MpesaWebClientBean;

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
//		Map<String,Object> req =  new HashMap<>();
//		req.put("BusinessShortCode",shortCode);
//		req.put("Password",password);
//		req.put("Timestamp",System.currentTimeMillis());
//		req.put("TransactionType", req);
//		req.put("PartyA", req);
//		req.put("PartyB", req);
//		req.put("PhoneNumber", req);
//		req.put("CallBackURL", req);
//		req.put("AccountReference", req);
//		req.put("TransactionDesc", req);
//		
//		var body =  new Gson().toJson(req);
		
		var token = this.authService.getMpesaAccessToken();
		
		return token;
	}
}
