package net.sasakonnect.wifi_portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WebClientConfig {
	@Bean
     WebClient webClient() {
        return WebClient.builder().baseUrl("https://sandbox.safaricom.co.ke").build();
    }
}
