package net.sasakonnect.wifi_portal.aspect;

import java.util.Base64;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import net.sasakonnect.wifi_portal.enums.JwtType;

@Aspect
@Component
public class BackOfficeAuthFilterAspect {
	
	@Around("@within(net.sasakonnect.wifi_portal.annotations.BackOfficeAuthFilter)")
	public Object applyBackofficeFilter(ProceedingJoinPoint joinPoint)  throws Throwable{
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	    String authHeader = request.getHeader("Authorization");
	    if(authHeader == null || authHeader.isEmpty()) {
	        return new ResponseEntity<>("Authorization header is missing",HttpStatus.UNAUTHORIZED);
	    }
	    if (!authHeader.startsWith("Bearer ")) {
	        return new ResponseEntity<>("Invalid token",HttpStatus.UNAUTHORIZED);
	    }
	    String token = authHeader.substring("Bearer ".length());
	    if(!this.getTokenTypeFromJwt(token).equalsIgnoreCase(JwtType.ADMIN_ACCESS_TOKEN.getToken())) {
	    	  return new ResponseEntity<>("Invalid token",HttpStatus.UNAUTHORIZED);
	    }
	    
	    return joinPoint.proceed();
	}
	
	
	public  String getTokenTypeFromJwt(String token) {
        try {
            // JWT format: header.payload.signature -> Split and get payload (index 1)
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;  // Invalid JWT
            }

            // Decode Base64 payload
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

            // Convert JSON to Map
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> payload = mapper.readValue(payloadJson, Map.class);

            // Return token_type if present
            return payload.get("token_type") != null ? payload.get("token_type").toString() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
