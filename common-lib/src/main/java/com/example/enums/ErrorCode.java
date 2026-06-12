package com.example.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    
    // 400 BAD REQUEST
    
    INVALID_REQUEST(400, "Invalid request"),
    INVALID_INPUT(400, "Invalid input"),
    INVALID_EMAIL(400, "Invalid email format"),
    INVALID_PASSWORD(400, "Invalid password"),
    INVALID_PHONE(400, "Invalid phone number"),
    INVALID_ROLE(400, "Invalid role"),
    PASSWORD_NOT_MATCH(400, "Password confirmation does not match"),
    CAPTCHA_INVALID(400, "Invalid captcha"),
    OTP_INVALID(400, "Invalid OTP"),
    OTP_EXPIRED(400, "OTP expired"),
    OTP_VERIFY_FAILED(400, "OTP verification failed"),
    OTP_NOT_FOUND(400, "OTP not found"),
    OLD_PASSWORD_INCORRECT(400, "Old password is incorrect"),
    NEW_PASSWORD_DUPLICATE(400, "New password cannot be the same as the old password"),
    EXPORT_FAILED(400, "Failed to export data"),
    FILE_NOT_FOUND(400, "File not found"),
    FILE_TOO_LARGE(400, "File size exceeds the limit"),
    UNSUPPORTED_FILE_TYPE(400, "Unsupported file type, Only JPG, PNG, WEBP are allowed"),
    USER_CREATE_FAILED(400, "User creation failed"),
    OTP_SAVE_FAILED(400, "OTP save failed"),

    
    // 401 UNAUTHORIZED
    
    UNAUTHENTICATED(401, "Unauthenticated"),
    AUTHENTICATION_FAILED(401, "Authentication failed"),
    INVALID_TOKEN(401, "Invalid token"),
    EXPIRED_TOKEN(401, "Token expired"),
    ACCESS_TOKEN_EXPIRED(401, "Access token expired"),
    REFRESH_TOKEN_EXPIRED(401, "Refresh token expired"),
    INVALID_REFRESH_TOKEN(401, "Invalid refresh token"),
    LOGIN_REQUIRED(401, "Login required"),

    
    // 403 FORBIDDEN
    
    ACCESS_DENIED(403, "Access denied"),
    USER_DISABLED(403, "User account is disabled"),
    USER_LOCKED(403, "User account is locked"),
    USER_NOT_VERIFIED(403, "User account is not verified"),
    BLACKLISTED_TOKEN(403, "Token has been blacklisted"),

    
    // 404 NOT FOUND
    
    USER_NOT_FOUND(404, "User not found"),
    ROLE_NOT_FOUND(404, "Role not found"),
    NOTIFICATION_NOT_FOUND(404, "Notification not found"),
    RESOURCE_NOT_FOUND(404, "Resource not found"),
    REFRESH_TOKEN_NOT_FOUND(404, "Refresh token not found"),

    
    // 409 CONFLICT
    
    USER_EXISTED(409, "User already exists"),
    EMAIL_ALREADY_USED(409, "Email already in use"),
    PHONE_ALREADY_USED(409, "Phone number already in use"),
    USER_ALREADY_VERIFIED(409, "User already verified"),

    
    // 429 TOO MANY REQUESTS
    
    TOO_MANY_REQUESTS(429, "Too many requests"),

    
    // 500 INTERNAL SERVER ERROR
    
    INTERNAL_SERVER_ERROR(500, "Internal server error"),
    DATABASE_ERROR(500, "Database error"),
    CACHE_WRITE_FAILED(500, "Failed to write cache"),
    MESSAGE_QUEUE_ERROR(500, "Message queue processing failed"),


    
    // 502 BAD GATEWAY
    
    MAIL_SERVER_ERROR(502, "Mail server error"),

    
    // 503 SERVICE UNAVAILABLE
    
    REDIS_CONNECTION_FAILED(503, "Redis service unavailable"),
    KAFKA_PUBLISH_FAILED(503, "Kafka publish failed"),
    KAFKA_CONSUME_FAILED(503, "Kafka consume failed"),
    EMAIL_SEND_FAILED(503, "Email service unavailable"),
    SMS_SEND_FAILED(503, "SMS service unavailable"),
    PUSH_NOTIFICATION_FAILED(503, "Push notification service unavailable"),
    STORAGE_ERROR(503, "Storage operation failed"),
    UPLOAD_FAILED(503, "Upload failed"),
    DELETE_FAILED(503, "Delete failed"),
    GENERATE_PRESIGNED_URL_FAILED(503, "Generate presigned URL failed");


    int code;
    String message;

}
