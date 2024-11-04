package net.sasakonnect.wifi_portal.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.sasakonnect.wifi_portal.services.AuthService;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class PortalWebClientBean {
	@Autowired
	AuthService authService;

	@PostConstruct
	public void portalWebClient() {
		this.webClient = WebClient.builder()
				.codecs(configurer -> configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder()))
				.defaultHeader("Authorization", "Basic " + authService.getBasicAuth())
				.filter(logRequest())
				.build();
	}

	public WebClient webClient;
	public Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	
	private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> 
                values.forEach(value -> log.info("Header: {} = {}", name, value))
            );
            // We cannot directly access the body here, so log it separately when constructing the request.
            return Mono.just(clientRequest);
        });
    }
}
