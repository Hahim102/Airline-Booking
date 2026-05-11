package com.example.service;

import com.example.payload.dto.PasswordDTO;
import com.example.payload.dto.UserDTO;
import com.example.payload.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    AuthResponse login(String email, String password) throws Exception;
    AuthResponse register(UserDTO request) throws Exception;
    void logout(String accessToken, String refreshToken);
    AuthResponse updateProfile(Long userId, UserDTO userDTO) throws Exception;
    AuthResponse refresh(String refreshToken) throws Exception;
    void updatePassword(Long userId, PasswordDTO passwordDTO) throws Exception;
}
