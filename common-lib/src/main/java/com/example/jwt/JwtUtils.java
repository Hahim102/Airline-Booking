package com.example.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.List;


@Component
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

    public static List<String> extractRoles(String token) {
        List<?> roles = extractAllClaims(token).get("roles", List.class);

        return roles.stream()
                .map(Object::toString)
                .toList();
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