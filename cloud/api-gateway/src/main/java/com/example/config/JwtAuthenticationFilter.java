package com.example.config;

import com.example.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;



    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
         String path = exchange.getRequest().getURI().getPath();
        List<String> publicEndpoints = List.of(
                "/api/auth/login",
                "/api/auth/register",
                "/api/auth/refresh"
        );

        if (publicEndpoints.contains(path)) {
            return chain.filter(exchange);
        }
         String authHeader = exchange.getRequest()
                 .getHeaders()
                 .getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorized(exchange);
            }

            String token = authHeader.substring(7);

            String blacklistKey =
                    "auth:blacklist:" + token;

        return reactiveRedisTemplate
                .hasKey(blacklistKey)
                .flatMap(exists -> {

                    if (Boolean.TRUE.equals(exists)) {

                        return unauthorized(exchange);
                    }

                    if (!JwtUtils.isTokenValid(token)) {

                        return unauthorized(exchange);
                    }

                    Long userId =
                            JwtUtils.extractUserId(token);

                    List<String> roles =
                            JwtUtils.extractRoles(token);

                    String email =
                            JwtUtils.extractEmail(token);

                    ServerHttpRequest request = exchange
                            .getRequest()
                            .mutate()
                            .header(
                                    "X-User-Email",
                                    email
                            )
                            .header(
                                    "X-User-Id",
                                    String.valueOf(userId)
                            )
                            .header(
                                    "X-User-Roles",
                                    String.join(",", roles)
                            )
                            .build();

                    return chain.filter(
                            exchange
                                    .mutate()
                                    .request(request)
                                    .build()
                    );
                });

    }

    public Mono<Void> unauthorized(ServerWebExchange exchange) {
         exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
         return exchange.getResponse().setComplete();
    }

}
