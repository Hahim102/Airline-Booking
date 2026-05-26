package com.example.controller;

import com.example.client.UserClient;
import com.example.config.UserPrincipal;
import com.example.enums.StatisticType;
import com.example.enums.SuccessCode;
import com.example.payload.response.ApiResponse;
import com.example.payload.response.ResponseUtils;
import com.example.payload.response.UserRegistrationStatsResponse;
import com.example.payload.response.UserSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final UserClient userClient;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> getSummary() {
        UserSummaryResponse userSummaryResponse = userClient
                .getUserSummary()
                .getData();

        return ResponseUtils.success(SuccessCode.SUCCESS, userSummaryResponse);
    }

    @GetMapping("/registrations")
    public ResponseEntity<ApiResponse<List<UserRegistrationStatsResponse>>> getRegistrations(
            @RequestParam(defaultValue = "DAY") StatisticType type
    ) {
        List<UserRegistrationStatsResponse> userRegistrationStats = userClient
                .getUserRegistrationStats(type)
                .getData();

        return ResponseUtils.success(SuccessCode.SUCCESS, userRegistrationStats);
    }

}
