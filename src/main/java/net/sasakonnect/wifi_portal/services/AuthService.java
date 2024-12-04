package net.sasakonnect.wifi_portal.services;

import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Cipher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import net.sasakonnect.wifi_portal.beans.DefaultWebClientBean;
import net.sasakonnect.wifi_portal.config.WebClientBean;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AuthService {
	@Value("classpath:static/mpesa_ssl_cert.cer")
	Resource mpesaSSL;
	@Autowired
	WebClientBean webClient;

	@Autowired
	DefaultWebClientBean client;
	@Value("${subsDevId}")
	String subsDevId;
	@Value("${portalUserName}")
	private String portalUserName;

	@Value("${portalUserPassword}")
	private String portalUserPassword;

	@Value("${consumerKey}")
	private String consumerKey;

	@Value("${shortCode}")
	private String shortCode;

	@Value("${consumerSecret}")
	private String consumerSecret;

	@Value("${consumerPassKey}")
	String consumerPassKey;
	//bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919

	public String getBasicAuth() {
		String data = portalUserName + ":" + portalUserPassword;
		byte[] encodedBytes = Base64.getEncoder().encode(data.getBytes());
		return new String(encodedBytes);
	}

	private String getMpesaBasicAuth() {
		String data = consumerKey + ":" + consumerSecret;
		byte[] encodedBytes = Base64.getEncoder().encode(data.getBytes());
		return new String(encodedBytes);
	}
	public String getMpesaMerchantPassword(String timestamp) {
		String data = shortCode +consumerPassKey+timestamp;
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


	public String generateSecurityCredential(String password) throws Exception {
		PublicKey publicKey = getPublicKeyFromCertificate(mpesaSSL);
		return encryptPasswordWithPublicKey(password, publicKey);
	}

	// Helper method to load the public key from the certificate
	private PublicKey getPublicKeyFromCertificate(Resource certificateResource) throws Exception {
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

		// Use Resource to get an InputStream
		try (InputStream inputStream = certificateResource.getInputStream()) {
			X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(inputStream);
			return certificate.getPublicKey();
		}
	}

	// Helper method to encrypt the password with the public key
	private String encryptPasswordWithPublicKey(String password, PublicKey publicKey) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);

		// Convert the password to bytes and encrypt
		byte[] encryptedBytes = cipher.doFinal(password.getBytes("UTF-8"));

		// Encode the encrypted bytes to Base64
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}




}
