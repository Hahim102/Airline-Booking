package com.example.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum SuccessCode {

    // 200 OK

    SUCCESS(200, "Success"),
    LOGIN_SUCCESS(200, "Login successful"),
    LOGOUT_SUCCESS(200, "Logout successful"),
    REFRESH_TOKEN_SUCCESS(200, "Token refreshed successfully"),
    PROFILE_UPDATED(200, "Profile updated successfully"),
    PASSWORD_UPDATED(200, "Password updated successfully"),
    PASSWORD_RESET_SUCCESS(200, "Password reset successfully"),
    OTP_VERIFIED(200, "OTP verified successfully"),
    EMAIL_SENT_SUCCESS(200, "Email sent successfully"),
    CONFIRM_RESET_PASSWORD_SUCCESS(200,  "OTP verified successfully. You can now reset your password."),
    OTP_SENT_SUCCESS(200, "OTP sent successfully"),

    // 201 CREATED

    USER_REGISTERED(201, "User registered successfully"),
    USER_CREATED(201, "User created successfully"),
    BOOKING_CREATED(201, "Booking created successfully"),
    NOTIFICATION_CREATED(201, "Notification created successfully"),

    // 202 ACCEPTED

    EMAIL_PROCESSING(202, "Email request accepted and processing"),
    NOTIFICATION_PROCESSING(202, "Notification request accepted and processing"),
    OTP_PROCESSING(202, "OTP request accepted and processing"),

    // 204 NO CONTENT

    USER_DELETED(204, "User deleted successfully"),
    BOOKING_CANCELLED(204, "Booking cancelled successfully");

    int code;
    String message;
    
}
