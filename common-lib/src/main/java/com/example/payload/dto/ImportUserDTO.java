package com.example.payload.dto;

import lombok.Data;

@Data
public class ImportUserDTO {
    private String email;
    private String fullName;
    private String phone;
    private String role;
}
