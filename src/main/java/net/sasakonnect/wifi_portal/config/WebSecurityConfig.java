package net.sasakonnect.wifi_portal.config;
import java.util.Arrays;

import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.swagger.v3.oas.models.parameters.HeaderParameter;
import lombok.extern.slf4j.Slf4j;
import net.sasakonnect.wifi_portal.services.UserService;

@Configuration
@EnableMethodSecurity
@Slf4j
@EnableAspectJAutoProxy

public class WebSecurityConfig {

	UserService userService;
	JwtAuthenticationFilter jwtAuthenticationFilter;
	

	WebSecurityConfig(UserService userService, JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.userService = userService;
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Autowired
	private ResourceLoader resourceLoader;

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	CorsConfigurationSource corsFilter() {
		CorsConfiguration configuration = new CorsConfiguration();

		// Specify the allowed origins (replace "*" with your specific origin)
		configuration.setAllowedOrigins(
				Arrays.asList("https://mfood.sasakonnect.net", "http://localhost:3000"));

		// Specify the allowed HTTP methods (e.g., GET, POST, PUT, DELETE)
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

		// Specify the allowed headers (e.g., Content-Type, Authorization)
		configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization"));

		// Allow credentials (e.g., cookies)
		configuration.setAllowCredentials(true);
		
		
		// Ensure the `Access-Control-Allow-Origin` header is sent
	    configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Authorization"));


		// Set max age (in seconds) for preflight requests
		configuration.setMaxAge(3600L); // 1 hour

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;

	}

	@Bean
	@Order(1)
	SecurityFilterChain auth0FilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests((authz) -> authz.requestMatchers("api-docs/**", // Swagger API documentation
				"/swagger-ui/**", // Swagger UI web interface
				"/swagger-resources/**",
				"swagger-config",
				// Swagger resources like JS and CSS
				"/webjars/**").permitAll()
				.requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
				.requestMatchers(
						"/portal/user/refresh/token",
						"/portal/sendOTP","/portal/confirmOTP",
						"/portal/register","/portal/host/cridentials",
						"/portal/confirmOTPV2","/payment/callBack",
						"/payment/confirm","/payment/callbackResolver",
						"/payment/result",
//						"/portal/getSubscriptionpackages",
						"/payment/mpesa/confirmTransaction",
						"/user/login")
				.permitAll()
//				.requestMatchers("/payment/**")
//				.permitAll()
//				.requestMatchers("/device/**")
//				.permitAll()
				.requestMatchers("/sdk/**").permitAll()
				.requestMatchers("/notification/send").permitAll()
				.requestMatchers("/views/**").permitAll()
                .requestMatchers("/utility/**").permitAll()		
                .requestMatchers(HttpMethod.OPTIONS, "/**")
				.permitAll() // Permit OPTIONS requests
				.anyRequest().authenticated()
		);
		http.httpBasic(basic -> basic.disable());
		http.csrf(csrf -> csrf.disable());
		http.cors(cors -> cors.disable());
		http.headers(headers -> headers.disable());

		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();

	}



	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	static MethodSecurityExpressionHandler expressionHandler(UserService userService) {
		var expressionHandler = new DefaultMethodSecurityExpressionHandler();
		expressionHandler.setPermissionEvaluator(new CustomPermissionEvaluator(userService));
		return expressionHandler;
	}



//	private SecurityScheme createAPIKeyScheme() {
//		return new SecurityScheme().type(SecurityScheme.Type.HTTP).bearerFormat("JWT").scheme("bearer");
//	}

	@Bean
	 WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowedMethods("GET", "POST", "PUT", "DELETE")
				        .allowedOrigins("*")
						.allowedHeaders("*");
			}
		};
	}


	
	@Bean
	GlobalOpenApiCustomizer globalOpenApiConstomizer() {
		Object example_token = "bearertoken";
		return openApi -> {
			openApi.getPaths().forEach((path, pathItem) -> {
				pathItem.readOperations().forEach(operation -> {
					if ("/portal/user/refresh/token".equals(path)) {
						operation.addParametersItem(
								new HeaderParameter().name("refresh-token-header")
										.allowEmptyValue(false).example("ej....").required(false));
						return;
					}
				});
			});
		};

	}

	@Bean
	ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		// Enable pretty-printing for JSON output
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		return objectMapper;
	}
	
	@Bean
	FilterRegistrationBean<RequestContextFilter> requestContextFilter() {
		FilterRegistrationBean<RequestContextFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new RequestContextFilter());
		registrationBean.addUrlPatterns("/*");
		return registrationBean;
	}
	@Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();  // Use the builder to create the RestTemplate instance
    }


}
