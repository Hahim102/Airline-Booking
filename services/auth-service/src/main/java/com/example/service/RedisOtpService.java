package com.example.service;

public interface RedisOtpService {

    void saveVerifyOtp(String email, String otp);

    String getVerifyOtp(String email);

    void deleteVerifyOtp(String email);


    void saveResetPasswordOtp(String email, String otp);
    String getResetPasswordOtp(String email);
    void deleteResetPasswordOtp(String email);
    
    void markResetPasswordOtpAsVerified(String email);
    boolean isResetPasswordOtpVerified(String email);
}
