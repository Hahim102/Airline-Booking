package com.example.payload.dto;

import lombok.Data;

@Data
public class ResetPasswordDTO {
    private String email;
    private String otp;
    private String newPassword;
}
