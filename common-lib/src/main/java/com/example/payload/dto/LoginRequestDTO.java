package com.example.payload.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {

    @NotBlank(message = "The email address cannot be left blank!")
    private String email;
    @NotBlank(message = "The password cannot be left blank!")
    private String password;
    private String captchaToken;
}
