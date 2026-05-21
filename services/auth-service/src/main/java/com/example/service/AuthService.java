package com.example.service;

import com.example.payload.dto.*;
import com.example.payload.response.AuthResponse;

public interface AuthService {

    AuthResponse login(String email, String password);
    AuthResponse register(UserDTO request);
    void logout(String accessToken, String refreshToken);
    AuthResponse updateProfile(Long id, UserDTO userDTO);
    AuthResponse refresh(String refreshToken);
    void updatePassword(Long userId, PasswordDTO passwordDTO);
    void verifyOtp(VerifyOtpDTO request);

    void forgotPassword(ForgotPasswordDTO request);
    
    void confirmResetPassword(VerifyOtpDTO request);

    void resetPassword(ResetPasswordDTO request);
}
