package com.example.service;

import com.example.payload.dto.*;
import com.example.payload.response.AuthResponse;
import com.example.payload.response.CreateUserResponse;

public interface AuthService {

    AuthResponse login(String email, String password);
    void register(UserDTO request);
    CreateUserResponse createUserByAdmin(CreateUserByAdminDTO request);
    void resendOtp(String email);
    void logout(String accessToken, String refreshToken);
//    AuthResponse updateProfile(Long id, UserDTO userDTO);
    AuthResponse refresh(String refreshToken);
    void updatePassword(Long userId, PasswordDTO passwordDTO);
    void verifyOtp(VerifyOtpDTO request);

    void forgotPassword(ForgotPasswordDTO request);
    void resendForgotOtp(String email);
    
    void confirmResetPassword(VerifyOtpDTO request);

    void resetPassword(ResetPasswordDTO request);
}
