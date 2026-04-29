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
        String roles = populateAuthorities(authorities);
        return Jwts.builder()
                .subject(authentication.getName())
                .claim("roles", roles)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))
                .signWith(secretKey)
                .compact();
    }

    public String populateAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<String> roles = new HashSet<>();
        for (GrantedAuthority authority : authorities) {
            roles.add(authority.getAuthority());
        }
        return String.join(",", roles);
    }
}
