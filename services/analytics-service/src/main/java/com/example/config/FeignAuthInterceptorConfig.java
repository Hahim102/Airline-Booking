package com.example.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class FeignAuthInterceptorConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {

            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                return;
            }

            UserPrincipal principal =
                    (UserPrincipal) authentication.getPrincipal();

            template.header("X-User-Id",
                    principal != null ? principal.getId().toString() : null);

            template.header("X-User-Email",
                    principal != null ? principal.getEmail() : null);

            template.header("X-User-Roles",
                    principal != null ? principal.getRoles() : null);
        };
    }
}
