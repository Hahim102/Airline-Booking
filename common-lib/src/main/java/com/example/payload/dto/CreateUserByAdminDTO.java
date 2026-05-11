package com.example.payload.dto;

import com.example.enums.UserRole;
import lombok.Data;

@Data
public class CreateUserByAdminDTO {
    private String email;
    private String fullName;
    private String phone;
    private UserRole role;
}
