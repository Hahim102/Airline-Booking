package com.example.payload.request;

import com.example.enums.UserRole;
import lombok.Data;

@Data
public class CreateUserRequest {

    private String email;
    private String password;
    private String fullName;
    private String phone;
    private UserRole role;

}
