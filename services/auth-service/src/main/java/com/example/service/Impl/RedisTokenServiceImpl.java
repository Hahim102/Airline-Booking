package com.example.service.Impl;

import com.example.service.RedisTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
@RequiredArgsConstructor
public class RedisTokenServiceImpl implements RedisTokenService {

    private static final String REFRESH_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void saveRefreshToken(Long userId,
                                 String refreshToken,
                                 long expiration) {

        String key = REFRESH_PREFIX + userId;

        redisTemplate.opsForValue().set(
                key,
                refreshToken,
                Duration.ofMillis(expiration)
        );

        String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(
                tokenKey,
                userId.toString(),
                Duration.ofMillis(expiration)
        );
    }

    @Override
    public String getRefreshToken(Long userId) {

        String key = REFRESH_PREFIX + userId;

        Object value = redisTemplate.opsForValue().get(key);

        return value != null ? value.toString() : null;
    }

    @Override
    public void deleteRefreshToken(Long userId) {

        redisTemplate.delete(REFRESH_PREFIX + userId);
    }

    @Override
    public void blacklistAccessToken(String token,
                                     long expiration) {

        String key = BLACKLIST_PREFIX + token;

        redisTemplate.opsForValue().set(
                key,
                "blacklisted",
                Duration.ofMillis(expiration)
        );
    }

    @Override
    public boolean isBlacklisted(String token) {

        return Boolean.TRUE.equals(
                redisTemplate.hasKey(BLACKLIST_PREFIX + token)
        );
    }

    @Override
    public boolean isRefreshTokenValid(String refreshToken) {
        String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(tokenKey)
        );
    }

    @Override
    public Long getUserIdFromRefreshToken(String refreshToken) {
        String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        Object value = redisTemplate.opsForValue().get(tokenKey);
        if (value != null) {
            try {
                return Long.parseLong(value.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public void revokeRefreshToken(String refreshToken) {
        String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        
        Long userId = getUserIdFromRefreshToken(refreshToken);
        
        redisTemplate.delete(tokenKey);
        if (userId != null) {
            redisTemplate.delete(REFRESH_PREFIX + userId);
        }
    }
}
