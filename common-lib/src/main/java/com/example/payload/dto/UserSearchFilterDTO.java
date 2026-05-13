package com.example.payload.dto;

import com.example.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchFilterDTO {
    private String fullName;
    private String email;
    private String phone;
    private UserRole role;
    private Boolean isActive;
    private int pageNumber;
    private int pageSize;
    private String sortBy;
    private String sortOrder;
}

