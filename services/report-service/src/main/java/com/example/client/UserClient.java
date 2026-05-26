package com.example.client;

import com.example.config.FeignAuthInterceptorConfig;
import com.example.payload.response.ApiResponse;
import com.example.payload.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@Component
@FeignClient(name = "USER-SERVICE", path = "/api/users", configuration = FeignAuthInterceptorConfig.class)
public interface UserClient {

    @GetMapping("/export-data")
    ApiResponse<List<UserResponse>> getUsersForExport();
}
