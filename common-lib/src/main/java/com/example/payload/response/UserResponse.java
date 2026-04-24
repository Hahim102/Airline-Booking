package com.example.payload.response;


import com.example.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String email;
    private String fullName;
    private String phone;
    private UserRole role;
    private LocalDateTime lastLoginAt;
}
