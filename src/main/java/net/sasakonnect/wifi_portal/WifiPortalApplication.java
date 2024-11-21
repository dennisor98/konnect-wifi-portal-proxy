package net.sasakonnect.wifi_portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableAspectJAutoProxy

@EnableRetry
public class WifiPortalApplication {

	public static void main(String[] args) {
		SpringApplication.run(WifiPortalApplication.class, args);
	}
	
	

}
