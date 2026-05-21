package com.example.payload.response;

import com.example.enums.SuccessCode;
import org.springframework.http.ResponseEntity;

public class ResponseUtils {
    public static <T> ResponseEntity<ApiResponse<T>> success(
            SuccessCode successCode,
            T data
    ) {

        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(true)
                .code(successCode.getCode())
                .message(successCode.getMessage())
                .data(data)
                .build();

        return ResponseEntity
                .status(successCode.getCode())
                .body(response);
    }

}
