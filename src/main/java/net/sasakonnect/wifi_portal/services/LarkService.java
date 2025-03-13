package net.sasakonnect.wifi_portal.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.Data;



@Data
class AccesTokenReponse {
	Integer code;
	Double expire;
	String msg;
	String tenant_access_token;
	
}

@Service
public class LarkService {
	@Value("${lark.bot.app.id}")
	String botAppId;
	
	@Value("${lark.bot.app.secret}")
	String botAppSecret;
	
	String larkGroup = "oc_f11965f2d1af0ecb6e39c29ff7beec86";
	protected final String larkBaseUrl = "https://open.larksuite.com/open-apis";
	  public void sendPaymentNotification(String message) {
		  var urlEndpoint = this.larkBaseUrl+"/message/v4/send/";
		  Map<String, Object> card = new HashMap<>();
		    card.put("msg_type", "interactive");
		    card.put("chat_id",larkGroup);
		    card.put("update_multi", false);
		    Map<String, Object> cardObj = new HashMap<>();
		    Map<String, Object> config = new HashMap<>();
		    config.put("wide_screen_mode", true);
		    cardObj.put("config", config);
		    Map<String, Object> div = new HashMap<>();
		    div.put("tag", "div");

		    Map<String, Object> field1 = new HashMap<>();
		    field1.put("is_short", true);
		    Map<String, Object> text1 = new HashMap<>();
		    text1.put("tag", "lark_md");
		    text1.put("content",message.replace(",", ""));
		    field1.put("text", text1);

		    Map<String, Object> field2 = new HashMap<>();
		    field2.put("is_short", false);
		    Map<String, Object> text2 = new HashMap<>();
		    text2.put("tag", "lark_md");
		    text2.put("content", "");
		    field2.put("text", text2);

		    div.put("fields", Arrays.asList(field1, field2));
		    cardObj.put("elements", Arrays.asList(div));

		    Map<String, Object> headerMap = new HashMap<>();
		    headerMap.put("template", "blue");
		    Map<String, Object> title = new HashMap<>();
		    title.put("tag", "plain_text");
		    title.put("content", "Pending Payment for followup");
		    headerMap.put("title", title);
		    cardObj.put("header", headerMap);
		    card.put("card", cardObj);

		    		
		    RestTemplate restTemplate = new RestTemplate();
		    HttpHeaders headers = new HttpHeaders();
		    headers.setContentType(MediaType.APPLICATION_JSON);
		    headers.set("Authorization", "Bearer "+this.getBotToken(botAppId, botAppSecret));   
		    HttpEntity<Object> requestEntity = new HttpEntity<>(card,headers);
//		    log.info("{reply response}"+urlEndpoint+event.getOpen_message_id()+"/reply");		        		   


		    ResponseEntity<Object> responseEntity = restTemplate.exchange(
		    		urlEndpoint.toString(),
		    		HttpMethod.POST,
		    		requestEntity,
		    		Object.class
		    		);
		    	
}

	  public String getBotToken(String appId,String appSecret) {
			RestTemplate restTemplate = new RestTemplate();
	        Map<String,Object> requestBody = new HashMap<>();
	        requestBody.put("app_id",appId);
	        requestBody.put("app_secret",appSecret);
	        
	        // Create HttpEntity with headers and body
	        HttpEntity<Map<String,Object>> requestEntity = new HttpEntity<>(requestBody);
	       try {
	    	    ResponseEntity<AccesTokenReponse> responseEntity = restTemplate.exchange(
	                this.larkBaseUrl+"/auth/v3/tenant_access_token/internal",
	                HttpMethod.POST,
	                requestEntity,
	                AccesTokenReponse.class
	        );
	    	    AccesTokenReponse responseBody =  responseEntity.getBody();
	    	    
	    	    return responseBody.tenant_access_token;
	       }catch(Exception ex) {
	    	 return null;
	       }
	       

	        
		}
}
