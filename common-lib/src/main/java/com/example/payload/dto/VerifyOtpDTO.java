package com.example.payload.dto;

import lombok.Data;

@Data
public class VerifyOtpDTO {

    private String email;

    private String otp;
}
