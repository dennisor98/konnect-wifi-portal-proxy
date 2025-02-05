package net.sasakonnect.wifi_portal.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@RestController
@Validated
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = { 
	    org.springframework.web.bind.annotation.RequestMethod.GET,
	    org.springframework.web.bind.annotation.RequestMethod.POST,
	    org.springframework.web.bind.annotation.RequestMethod.PUT,
	    org.springframework.web.bind.annotation.RequestMethod.DELETE,
	    org.springframework.web.bind.annotation.RequestMethod.OPTIONS
	})
public @interface CustomController {

}
