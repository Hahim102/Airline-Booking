package com.example.client;

import com.example.enums.StatisticType;
import com.example.payload.response.ApiResponse;
import com.example.payload.response.UserRegistrationStatsResponse;
import com.example.payload.response.UserSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserClient {
    private final WebClient userServiceWebClient;

    public UserSummaryResponse getUserSummary(String email, String roles) {
        ApiResponse<UserSummaryResponse> response =
                userServiceWebClient.get()
                        .uri("/api/users/statistics/summary")
                        .header("X-User-Email", email)
                        .header("X-User-Roles", roles)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserSummaryResponse>>() {})
                        .block();

        return response.getData();
    }

    public List<UserRegistrationStatsResponse> getUserRegistrationStats(
            String email,
            String roles,
            StatisticType type
    ) {
        ApiResponse<List<UserRegistrationStatsResponse>> response =
                userServiceWebClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/api/users/statistics/registrations")
                                .queryParam("type", type)
                                .build()
                        )
                        .header("X-User-Email", email)
                        .header("X-User-Roles", roles)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<UserRegistrationStatsResponse>>>() {})
                        .block();

        return response.getData();
    }
}
