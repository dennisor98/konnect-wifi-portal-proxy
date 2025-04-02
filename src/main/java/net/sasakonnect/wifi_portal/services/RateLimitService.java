package net.sasakonnect.wifi_portal.services;

import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;

@Service
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;
    private static final int MAX_REQUESTS = 10; // Limit per minute

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String ip) {
        String key = "rate_limit:" + ip;
        Long count = redisTemplate.opsForValue().increment(key, 1);

        if (count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }

        return count <= MAX_REQUESTS;
    }
}

