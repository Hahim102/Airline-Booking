package com.example.payload.dto;


import com.example.enums.UserRole;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "Email is not in the correct format."
    )
    private String email;

    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "The password must have at least 8 characters, " +
                    "including uppercase letters, lowercase letters, " +
                    "numbers, and special characters."
    )
    private String password;

    @NotBlank(message = "The full name must not be left blank.")
    @Pattern(
            regexp = "^(?!\\s*$).+",
            message = "Invalid name"
    )
    private String fullName;

    @Pattern(
            regexp = "^(0[3|5|7|8|9])[0-9]{8}$",
            message = "Invalid phone number"
    )
    private String phone;


    private String captchaToken;



}
