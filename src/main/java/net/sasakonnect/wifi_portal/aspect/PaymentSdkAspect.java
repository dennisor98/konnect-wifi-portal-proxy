package net.sasakonnect.wifi_portal.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import net.sasakonnect.wifi_portal.beans.AppRequestBean;
import net.sasakonnect.wifi_portal.beans.Two56BitKeyBean;
import net.sasakonnect.wifi_portal.services.AppService;
import net.sasakonnect.wifi_portal.services.PaymentService;

@Aspect
@Component
public class PaymentSdkAspect {
	@Autowired
	AppService paymentService;
	@Autowired
	AppRequestBean appRequestBean;
	

	@Around("@annotation(net.sasakonnect.wifi_portal.annotations.PaymentSdkFilter)")
	public Object applyCustomFilter(ProceedingJoinPoint joinPoint) throws Throwable {
		 HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	        // Retrieve specific header (for example, Authorization header)
		 
		  // Retrieve specific header (for example, Authorization header)
	        String appKey = request.getHeader("App-Key");   // Replace with your actual header name
	        String appSecret = request.getHeader("App-Secret");   // Replace with your actual header name

	        // Log headers for debugging
	        System.out.println("App-Key: " + appKey);
	        System.out.println("App-Secret: " + appSecret);

	        // Example condition to terminate method execution if headers are invalid
	        if (appKey == null || appSecret == null) {
	            System.out.println("App-Key or App-Secret is missing, terminating method execution.");
	            // Return a specific response (e.g., error message or default value) if needed
	            return new ResponseEntity<>("App-Key is missing", HttpStatus.BAD_REQUEST); // 400 Bad Request
 // or throw an exception
	        }

	        // Optionally call the service to find app
	       var app= this.paymentService.findAppByAppKeyAndSecret(appKey, appSecret);
	       
	        
	       if (app == null || app.isEmpty()) {
	           return new ResponseEntity<>("Error: App Not Found, App-Secret, App-Key did not match", HttpStatus.NOT_FOUND); // 404 Not Found
	       }

	    // Add any filtering logic here before or after method execution
	    System.out.println("Applying custom filter before method execution");
        this.appRequestBean.setApp(app.get());
	    // Proceed with method execution
	    Object result = joinPoint.proceed();
	   
	    // Add logic after method execution

	    return result;
	}

}
