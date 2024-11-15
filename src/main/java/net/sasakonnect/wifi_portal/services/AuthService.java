package net.sasakonnect.wifi_portal.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import net.sasakonnect.wifi_portal.config.WebClientBean;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AuthService {
	@Autowired
	WebClientBean webClient;

	@Value("${portalUserName}")
	private String portalUserName;

	@Value("${portalUserPassword}")
	private String portalUserPassword;

	@Value("${mpesaUserName}")
	private String mpesaUserName;

	@Value("${merchantAccount}")
	private String shortCode;

	@Value("${mpesaPassword}")
	private String mpesaPassword;

	private final Cache<String,String> tokenCache;

	public AuthService() {
		this.tokenCache = Caffeine.newBuilder()
				.maximumSize(1) 
				.build();
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
	public String getMpesaMerchantPassword(String timestamp) {
		String data = shortCode + ":" +"bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"+":"+timestamp;
		byte[] encodedBytes = Base64.getEncoder().encode(data.getBytes());
		return new String(encodedBytes);
	}


	public String getMpesaAccessToken() {
		var basicAuth = this.getMpesaBasicAuth();
		log.error(basicAuth);
		Mono<String> responseMono = this.webClient.webClient
				.get()
				.uri(uriBuilder -> uriBuilder
						.path("/oauth/v1/generate")
						.queryParam("grant_type", "client_credentials")
						.build())
				.header("Authorization", "Basic " + basicAuth)
				.retrieve()
				.bodyToMono(String.class);


		String responseJson = responseMono.block();
		if(responseJson !=null) {
			var response = new Gson().fromJson(responseJson,Map.class);

			var access_token = response.get("access_token").toString();
			log.error(access_token);
			return access_token;

		}

		return null;
	}



}
