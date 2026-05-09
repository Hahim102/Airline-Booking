package com.example.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    @Value("${app.auth.refresh-cookie.secure:false}")
    private boolean refreshCookieSecure;

    @Value("${app.auth.refresh-cookie.same-site:Lax}")
    private String refreshCookieSameSite;

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final int REFRESH_TOKEN_TTL_SECONDS = 7 * 24 * 60 * 60;


    public void addRefreshTokenCookie(HttpServletResponse response, String token, int maxAge) {
        ResponseCookie cookie = ResponseCookie
                .from(REFRESH_TOKEN_COOKIE, token)
                .maxAge(maxAge)
                .path("/")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }


    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie
                .from(REFRESH_TOKEN_COOKIE, "")
                .maxAge(0)
                .path("/")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }


    public String extractRefreshTokenFromCookies(jakarta.servlet.http.HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(REFRESH_TOKEN_COOKIE)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
