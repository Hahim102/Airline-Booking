package com.example.client;

import com.example.config.FeignAuthInterceptorConfig;
import com.example.enums.StatisticType;
import com.example.payload.response.ApiResponse;
import com.example.payload.response.UserRegistrationStatsResponse;
import com.example.payload.response.UserSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
@FeignClient(name = "USER-SERVICE", path = "/api/users", configuration = FeignAuthInterceptorConfig.class)
public interface UserClient {

    @GetMapping("/statistics/summary")
    ApiResponse<UserSummaryResponse> getUserSummary();

    @GetMapping("/statistics/registrations")
    ApiResponse<List<UserRegistrationStatsResponse>> getUserRegistrationStats(
            @RequestParam("type") StatisticType type
    );
}
