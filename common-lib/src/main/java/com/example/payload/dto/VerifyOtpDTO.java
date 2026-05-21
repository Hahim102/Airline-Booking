package com.example.payload.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpDTO {

    @NotBlank
    private String email;

    private String otp;
}
