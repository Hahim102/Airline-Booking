package com.example.service;


public interface RedisTokenService {

    void saveRefreshToken(Long userId, String refreshToken, long expiration);

    String getRefreshToken(Long userId);

    void deleteRefreshToken(Long userId);

    void blacklistAccessToken(String token, long expiration);

    boolean isBlacklisted(String token);

    boolean isRefreshTokenValid(String refreshToken);

    Long getUserIdFromRefreshToken(String refreshToken);

    void revokeRefreshToken(String refreshToken);
}


