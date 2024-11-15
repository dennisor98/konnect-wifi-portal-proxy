package net.sasakonnect.wifi_portal.beans;

import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;

@Component
public class DefaultWebClientBean {
	@PostConstruct
	public void defaultWebClientBean() {
		this.webClient = WebClient.builder()
				.codecs(configurer -> configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder()))
				.build();
	}

	public WebClient webClient;
	public Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
}
