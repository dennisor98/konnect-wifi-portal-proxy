package net.sasakonnect.wifi_portal.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sasakonnect.wifi_portal.services.RateLimitService;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

//@Component
public class RateLimitFilter implements Filter {

    @Autowired
    private RateLimitService rateLimitService;

    // List of Swagger-related paths to exclude from rate limiting
    private static final List<String> SWAGGER_WHITELIST = List.of(
        "/swagger-ui.html", 
        "/swagger-ui/**", 
        "/v3/api-docs/**", 
        "/swagger-resources/**", 
        "/webjars/**"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String ip = httpRequest.getRemoteAddr();
        String requestUri = httpRequest.getRequestURI();

        // Exclude Swagger-related paths
        if (isSwaggerRequest(requestUri)) {
            chain.doFilter(request, response);
            return;
        }

        // Apply rate limiting for other requests
        if (rateLimitService.isAllowed(ip)) {
            chain.doFilter(request, response);
        } else {
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\": \"Rate limit exceeded\"}");
            httpResponse.getWriter().flush();
        }
    }

    private boolean isSwaggerRequest(String requestUri) {
        return SWAGGER_WHITELIST.stream().anyMatch(requestUri::startsWith);
    }
}
