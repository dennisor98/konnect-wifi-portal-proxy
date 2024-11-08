package net.sasakonnect.wifi_portal.services;

import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AuthService {
	
	WebClient webClient;
	
	@Value("${portalUserName}")
	private String portalUserName;
	
	@Value("${portalUserPassword}")
	private String portalUserPassword;
	
	@Value("${mpesaUserName}")
	private String mpesaUserName;
	
	@Value("${mpesaPassword}")
	private String mpesaPassword;
	
	public AuthService(WebClient webClient){
       this.webClient = webClient;
	}
	  public String getBasicAuth() {
	       String data = portalUserName + ":" + portalUserPassword;
	       byte[] encodedBytes = Base64.getEncoder().encode(data.getBytes());
	       return new String(encodedBytes);
	   }
	  
	 private String getMpesaBasicAuth() {
		 String data = mpesaUserName + ":" + mpesaPassword;
	       byte[] encodedBytes = Base64.getEncoder().encode(data.getBytes());
	       return new String(encodedBytes);
	 }
	 
	 
	 public String getMpesaAccessToken() {
		 Mono<String> responseMono = this.webClient.get()
				 .uri(uriBuilder -> uriBuilder
						 .path("/oauth/v1/generate")
						 .queryParam("grant_type", "cridentials")
						 .build())
				 .accept(MediaType.APPLICATION_JSON)
				 .header("Authorization","Basic "+this.getMpesaBasicAuth())
				 .retrieve()
				 .bodyToMono(String.class);

		 String responseJson = responseMono.block();
		 log.error(responseJson);
		 if(responseJson !=null) {
			 var response = new Gson().fromJson(responseJson,Map.class);
			 
			 return response.get("access_token").toString();
			 		
		 }

		 return null;
	 }

	  
	  
}
