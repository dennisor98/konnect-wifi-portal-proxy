package net.sasakonnect.wifi_portal.config;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import net.sasakonnect.wifi_portal.aspect.RateLimitAspect;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(RateLimitAspect.RateLimitExceededException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public String handleRateLimitException(RateLimitAspect.RateLimitExceededException e) {
        return "{\"error\": \"Rate limit exceeded\"}";
    }
}
