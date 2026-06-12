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

    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";
    private static final String BLACKLIST_TOKEN_PREFIX = "auth:blacklist:";

    private String buildRefreshTokenKey(Long userId) {
        return REFRESH_TOKEN_PREFIX + userId;
    }

    private String buildBlacklistTokenKey(String accessToken) {
        return BLACKLIST_TOKEN_PREFIX + accessToken;
    }

    @Override
    public void saveRefreshToken(
            Long userId,
            String refreshToken,
            long expiration
    ) {
        redisTemplate.opsForValue().set(
                buildRefreshTokenKey(userId),
                refreshToken,
                Duration.ofMillis(expiration)
        );
    }

    @Override
    public String getRefreshToken(Long userId) {
        Object value = redisTemplate
                .opsForValue()
                .get(buildRefreshTokenKey(userId));

        return value == null ? null : value.toString();
    }

    @Override
    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(buildRefreshTokenKey(userId));
    }

    @Override
    public void blacklistAccessToken(
            String accessToken,
            long expiration
    ) {
        redisTemplate.opsForValue().set(
                buildBlacklistTokenKey(accessToken),
                "true",
                Duration.ofMillis(expiration)
        );
    }

    @Override
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(buildBlacklistTokenKey(token))
        );
    }
}
