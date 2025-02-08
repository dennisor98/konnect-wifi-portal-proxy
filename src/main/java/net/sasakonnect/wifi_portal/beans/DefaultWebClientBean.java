package net.sasakonnect.wifi_portal.beans;

import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

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

	  private ExchangeFilterFunction logRequest() {
	        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
	            System.out.println("Request: " + clientRequest.method() + " " + clientRequest.url());
	            clientRequest.headers().forEach((name, values) -> 
	                values.forEach(value -> System.out.println(name + ": " + value))
	            );
	            return Mono.just(clientRequest);
	        });
	    }
}
