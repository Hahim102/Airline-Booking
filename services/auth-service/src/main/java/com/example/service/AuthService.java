package com.example.service;

import com.example.payload.dto.UserDTO;
import com.example.payload.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    AuthResponse login(String username, String password,HttpServletResponse response) throws Exception;
    AuthResponse register(UserDTO request, HttpServletResponse response) throws Exception;
    void logout(HttpServletRequest request, HttpServletResponse response, String token);
    AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
