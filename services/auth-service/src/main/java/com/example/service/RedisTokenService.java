package com.example.service;

public interface RedisTokenService {
    void saveRefreshToken(
            Long userId,
            String refreshToken,
            long expiration);

    String getRefreshToken(Long userId);

    void deleteRefreshToken(Long userId);

    void blacklistAccessToken(
            String accessToken,
            long expiration);

    boolean isBlacklisted(String token);
}
