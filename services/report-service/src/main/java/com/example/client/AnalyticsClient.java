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
@FeignClient(name = "ANALYTICS-SERVICE", path = "/api/analytics", configuration = FeignAuthInterceptorConfig.class)
public interface AnalyticsClient {

    @GetMapping("/summary")
    ApiResponse<UserSummaryResponse> getUserSummary();

    @GetMapping("/registrations")
    ApiResponse<List<UserRegistrationStatsResponse>> getUserRegistrations();
}
