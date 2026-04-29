package com.example.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

public class JwtUtils {

    private static final SecretKey SECRET_KEY =
            Keys.hmacShaKeyFor(JwtConstant.JWT_SECRET.getBytes());

    public static Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public static String extractRoles(String token) {
        return extractAllClaims(token).get("roles", String.class);
    }

    public static Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    public static boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}