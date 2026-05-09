package com.example.payload.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExportUserDTO {
    private String email;
    private String password;
}
