package com.example.service;

import com.example.payload.dto.UserDTO;
import com.example.payload.response.AuthResponse;

public interface AuthService {

    AuthResponse login(String username, String password);
    AuthResponse register(UserDTO request) throws Exception;
}
