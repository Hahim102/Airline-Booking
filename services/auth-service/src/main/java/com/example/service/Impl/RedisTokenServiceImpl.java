package com.example.service.Impl;

import com.example.service.RedisTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisTokenServiceImpl
        implements RedisTokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void saveRefreshToken(
            Long userId,
            String refreshToken,
            long expiration) {

        String key = "auth:refresh:" + userId;

        redisTemplate.opsForValue().set(
                key,
                refreshToken,
                Duration.ofMillis(expiration)
        );
    }

    @Override
    public String getRefreshToken(Long userId) {

        String key = "auth:refresh:" + userId;

        return (String) redisTemplate
                .opsForValue()
                .get(key);
    }

    @Override
    public void deleteRefreshToken(Long userId) {

        String key = "auth:refresh:" + userId;

        redisTemplate.delete(key);
    }

    @Override
    public void blacklistAccessToken(
            String accessToken,
            long expiration) {

        String key =
                "auth:blacklist:" + accessToken;

        redisTemplate.opsForValue().set(
                key,
                "true",
                Duration.ofMillis(expiration)
        );
    }

    @Override
    public boolean isBlacklisted(String token) {

        String key =
                "auth:blacklist:" + token;

        return Boolean.TRUE.equals(
                redisTemplate.hasKey(key)
        );
    }
}
