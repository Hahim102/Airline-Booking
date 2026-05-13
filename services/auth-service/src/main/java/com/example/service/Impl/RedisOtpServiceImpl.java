package com.example.service.Impl;

import com.example.service.RedisOtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisOtpServiceImpl implements RedisOtpService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String OTP_PREFIX = "VERIFY_OTP:";
    private static final String RESET_PASSWORD_PREFIX = "RESET_PASSWORD_OTP:";
    private static final String RESET_PASSWORD_VERIFIED_PREFIX = "RESET_PASSWORD_VERIFIED:";

    private String buildKey(String email) {
        return OTP_PREFIX + email.trim().toLowerCase();
    }

    private String buildResetPasswordOtpKey(String email) {
        return RESET_PASSWORD_PREFIX + email.trim().toLowerCase();
    }

    private String buildResetPasswordVerifiedKey(String email) {
        return RESET_PASSWORD_VERIFIED_PREFIX + email.trim().toLowerCase();
    }

    @Override
    public void saveVerifyOtp(String email, String otp) {
        String key = buildKey(email);

        redisTemplate.delete(key);

        redisTemplate.opsForValue().set(
                key,
                otp,
                Duration.ofMinutes(5)
        );

        Object savedOtp = redisTemplate.opsForValue().get(key);

        System.out.println("SAVE OTP KEY = " + key);
        System.out.println("SAVE OTP VALUE = " + otp);
        System.out.println("REDIS AFTER SAVE = " + savedOtp);
    }

    @Override
    public String getVerifyOtp(String email) {
        String key = buildKey(email);

        Object value = redisTemplate.opsForValue().get(key);

        System.out.println("GET OTP KEY = " + key);
        System.out.println("GET OTP VALUE = " + value);

        return value == null ? null : value.toString();
    }

    @Override
    public void deleteVerifyOtp(String email) {
        redisTemplate.delete(buildKey(email));
    }

    @Override
    public void saveResetPasswordOtp(String email, String otp) {
        String key = buildResetPasswordOtpKey(email);
        redisTemplate.delete(key);

        redisTemplate.opsForValue().set(
                key,
                otp,
                Duration.ofMinutes(10)
        );
        Object savedOtp = redisTemplate.opsForValue().get(key);
        
        System.out.println("SAVE RESET PASSWORD OTP KEY = " + key);
        System.out.println("SAVE RESET PASSWORD OTP VALUE = " + otp);
    }

    @Override
    public String getResetPasswordOtp(String email) {
        String key = buildResetPasswordOtpKey(email);
        Object value = redisTemplate.opsForValue().get(key);

        System.out.println("GET RESET PASSWORD OTP KEY = " + key);
        System.out.println("GET RESET PASSWORD OTP VALUE = " + value);

        return value == null ? null : value.toString();
    }

    @Override
    public void deleteResetPasswordOtp(String email) {
        redisTemplate.delete(buildResetPasswordOtpKey(email));
        redisTemplate.delete(buildResetPasswordVerifiedKey(email));
    }

    @Override
    public void markResetPasswordOtpAsVerified(String email) {
        String verifiedKey = buildResetPasswordVerifiedKey(email);
        redisTemplate.opsForValue().set(
                verifiedKey,
                "true",
                Duration.ofMinutes(10)
        );
        System.out.println("MARKED RESET PASSWORD OTP AS VERIFIED FOR: " + email);
    }

    @Override
    public boolean isResetPasswordOtpVerified(String email) {
        String verifiedKey = buildResetPasswordVerifiedKey(email);
        Object value = redisTemplate.opsForValue().get(verifiedKey);
        boolean isVerified = value != null && value.toString().equals("true");
        System.out.println("CHECK RESET PASSWORD OTP VERIFIED FOR: " + email + " = " + isVerified);
        return isVerified;
    }
}
