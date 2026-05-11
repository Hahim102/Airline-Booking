package com.example.config;

import com.example.jwt.JwtConstant;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;


@Component
public class JwtProvider {

    private final SecretKey secretKey = Keys.hmacShaKeyFor(
            JwtConstant.JWT_SECRET.getBytes()
    );


    public String generateAccessToken(Authentication authentication, Long userId) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        List<String> roles = populateAuthorities(authorities);
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", authentication.getName())
                .claim("roles", roles)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + getAccessExpiration()))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + getRefreshExpiration()    ))
                .signWith(secretKey)
                .compact();
    }

    public List<String> populateAuthorities(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(r -> r.startsWith("ROLE_"))
                .toList();
    }
    public long getRefreshExpiration() {
        return 7 * 24 * 60 * 60 * 1000;
    }
    public long getAccessExpiration() {
        return 15 * 60 * 1000;
    }
}
