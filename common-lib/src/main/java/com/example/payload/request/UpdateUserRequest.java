package com.example.payload.request;

import com.example.enums.UserRole;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String email;
    private String fullName;
    private String phone;
    private UserRole role;
}
