package net.sasakonnect.wifi_portal.aspect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import jakarta.servlet.http.HttpServletRequest;
import net.sasakonnect.wifi_portal.annotations.RateLimit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Aspect
@Component
public class RateLimitAspect {

    private final RedisTemplate<String, String> redisTemplate;

    public RateLimitAspect(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Around("@annotation(rateLimit)")
    public Object limitRequests(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes == null) {
            return joinPoint.proceed(); // Allow execution if request context is unavailable
        }

        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteAddr();
        String route = request.getRequestURI();
        String txId = request.getParameter("txId");
        String key = "rate_limit:" + ip + ":" + route+"/"+txId; // Redis key (per IP per route)

        // Get current request count from Redis
        String countStr = redisTemplate.opsForValue().get(key);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);

        if (count >= rateLimit.maxRequests()) {
            throw new RateLimitExceededException();
        }

        // Increment count and set expiration if it's the first request
       redisTemplate.opsForValue().increment(key);
        if (count == 0) {
            redisTemplate.expire(key, rateLimit.durationSeconds(), TimeUnit.SECONDS);
        }

        return joinPoint.proceed();
    }

    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException() {
            super("Too many requests");
        }
    }
}
