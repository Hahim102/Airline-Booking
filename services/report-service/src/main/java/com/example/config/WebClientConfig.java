package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient userServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:5001")
                .build();
    }
    @Bean
    public WebClient analyticsServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:5004")
                .build();
    }
}
