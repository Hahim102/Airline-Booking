package com.example.service;

import com.example.payload.dto.*;
import com.example.payload.response.AuthResponse;

public interface AuthService {

    AuthResponse login(String email, String password) throws Exception;
    AuthResponse register(UserDTO request) throws Exception;
    void logout(String accessToken, String refreshToken);
    AuthResponse updateProfile(Long userId, UserDTO userDTO) throws Exception;
    AuthResponse refresh(String refreshToken) throws Exception;
    void updatePassword(Long userId, PasswordDTO passwordDTO) throws Exception;
    AuthResponse verifyOtp(VerifyOtpDTO request);

    AuthResponse forgotPassword(ForgotPasswordDTO request);
    
    AuthResponse confirmResetPassword(VerifyOtpDTO request);

    AuthResponse resetPassword(ResetPasswordDTO request);
}
