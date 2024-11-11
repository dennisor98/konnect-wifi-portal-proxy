package net.sasakonnect.wifi_portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


@Configuration
@Slf4j
public class WebClientBean {
	@PostConstruct
	public void portalWebClient() {
		this.webClient = WebClient.builder()
				.codecs(configurer -> configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder()))
				.baseUrl("https://sandbox.safaricom.co.ke")
				.filter(logRequest())
				.filter(logResponse())
				.build();
	}

	public WebClient webClient;
	public Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	
	private ExchangeFilterFunction logResponse() {
	    return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
	        // Log response status
	        log.info("Response Status: " + clientResponse.bodyToMono(String.class));
	        return Mono.just(clientResponse);
	    });
	}
	
	 private ExchangeFilterFunction logRequest() {
	        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
	            // Log request details
	            log.info("Request: " + clientRequest.method() + " " + clientRequest.url());
	            clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info(name + ": " + value)));
	            return Mono.just(clientRequest);
	        });
	    }


}
