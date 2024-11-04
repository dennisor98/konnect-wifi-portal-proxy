package net.sasakonnect.wifi_portal.services;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
	@Value("${portalUserName}")
	private String portalUserName;
	

	
	
	@Value("${portalUserPassword}")
	private String portalUserPassword;
	
	  public String getBasicAuth() {
	       String data = portalUserName + ":" + portalUserPassword;
	       byte[] encodedBytes = Base64.getEncoder().encode(data.getBytes());
	       return new String(encodedBytes);
	   }
	  
	  
}
