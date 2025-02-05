package net.sasakonnect.wifi_portal.aspect;

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

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class BackOfficeAuthFilterAspect {
	
	@Around("@annotation(net.sasakonnect.wifi_portal.annotations.BackOfficeAuthFilter)")
	public Object applyBackofficeFilter(ProceedingJoinPoint joinPoint)  throws Throwable{
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	    String authHeader = request.getHeader("Authorization");
	    if(authHeader == null || authHeader.isEmpty()) {
	        return new ResponseEntity<>("Authorization header is missing",HttpStatus.UNAUTHORIZED);
	    }
	    if (!authHeader.startsWith("Bearer ")) {
	        return new ResponseEntity<>("Invalid token type",HttpStatus.UNAUTHORIZED);
	    }
	    String token = authHeader.substring("Bearer ".length());
	    if(!token.startsWith("admin_access_token:")) {
	    	 return new ResponseEntity<>("Invalid token type",HttpStatus.UNAUTHORIZED);
	    }
	    Object result = joinPoint.proceed();
	    return result;
	}

}
