package net.sasakonnect.wifi_portal.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // Apply to methods only
@Retention(RetentionPolicy.RUNTIME) // Available at runtime
public @interface RateLimit {
	int maxRequests(); 
    int durationSeconds();
}
