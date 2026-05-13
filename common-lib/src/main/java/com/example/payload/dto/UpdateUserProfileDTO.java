package com.example.payload.dto;

import com.example.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileDTO {
    private String fullName;
    private String phone;
    private UserRole role;
}

