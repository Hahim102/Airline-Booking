package com.example.client;

import com.example.payload.response.ApiResponse;
import com.example.payload.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final WebClient userServiceWebClient;

    public List<UserResponse> getUsersForExport(String email, String roles) {
        ApiResponse<List<UserResponse>> response =
                userServiceWebClient.get()
                        .uri("/api/users/export-data")
                        .header("X-User-Email", email)
                        .header("X-User-Roles", roles)
                        .retrieve()
                        .bodyToMono(
                                new ParameterizedTypeReference<
                                        ApiResponse<List<UserResponse>>
                                        >() {}
                        )
                        .block();

        return response.getData();
    }
}
