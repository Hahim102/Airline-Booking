package com.example.service.Impl;

import com.example.service.RedisOtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

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

    }

    @Override
    public String getVerifyOtp(String email) {
        Object value = redisTemplate
                .opsForValue()
                .get(buildKey(email));

        return value == null ? null : value.toString();
    }

    @Override
    public void deleteVerifyOtp(String email) {
        redisTemplate.delete(buildKey(email));
    }



    @Override
    public void saveResetPasswordOtp(String email, String otp) {
        String otpKey = buildResetPasswordOtpKey(email);
        String verifiedKey = buildResetPasswordVerifiedKey(email);

        redisTemplate.delete(otpKey);
        redisTemplate.delete(verifiedKey);

        redisTemplate.opsForValue().set(
                otpKey,
                otp,
                Duration.ofMinutes(5)
        );
    }

    @Override
    public String getResetPasswordOtp(String email) {
        Object value = redisTemplate
                .opsForValue()
                .get(buildResetPasswordOtpKey(email));

        return value == null ? null : value.toString();
    }

    @Override
    public void markResetPasswordOtpAsVerified(String email) {
        redisTemplate.opsForValue().set(
                buildResetPasswordVerifiedKey(email),
                "true",
                Duration.ofMinutes(5)
        );
    }

    @Override
    public boolean isResetPasswordOtpVerified(String email) {
        Object value = redisTemplate
                .opsForValue()
                .get(buildResetPasswordVerifiedKey(email));

        return "true".equals(String.valueOf(value));
    }

    @Override
    public void deleteResetPasswordOtp(String email) {
        redisTemplate.delete(buildResetPasswordOtpKey(email));
        redisTemplate.delete(buildResetPasswordVerifiedKey(email));
    }

}
