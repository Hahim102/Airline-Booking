package com.example.controller;


import com.example.enums.ErrorCode;
import com.example.enums.SuccessCode;
import com.example.exception.AppException;
import com.example.payload.dto.*;
import com.example.payload.response.*;
import com.example.service.AuthService;
import com.example.service.Impl.RecaptchaService;
import com.example.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RecaptchaService recaptchaService;
    private final CookieUtils cookieUtils;
    private static final int REFRESH_TOKEN_TTL_SECONDS = 7 * 24 * 60 * 60;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody @Valid UserDTO userDTO,
                                                              HttpServletResponse response) {
        RecaptchaResponse captchaResponse =
                recaptchaService.verify(userDTO.getCaptchaToken());

        if (captchaResponse == null || !captchaResponse.isSuccess()) {
            throw new AppException(ErrorCode.CAPTCHA_INVALID);
        }
        authService.register(userDTO);
        return ResponseUtils.success(
                SuccessCode.USER_REGISTERED,
                null
        );
    }
        @PostMapping("/admin/create-user")
    public ResponseEntity<ApiResponse<CreateUserResponse>> createUserByAdmin(
            @RequestBody @Valid CreateUserByAdminDTO request
    ) {
        CreateUserResponse createdUser = authService.createUserByAdmin(request);
        return ResponseUtils.success(SuccessCode.USER_CREATED, createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody @Valid LoginRequestDTO request,
                                                           HttpServletResponse response) {
        RecaptchaResponse captchaResponse =
                recaptchaService.verify(request.getCaptchaToken());

        if (captchaResponse == null || !captchaResponse.isSuccess()) {
            throw new AppException(ErrorCode.CAPTCHA_INVALID);
        }

        AuthResponse result =
                authService.login(request.getEmail(), request.getPassword());

        cookieUtils.addRefreshTokenCookie(
                response,
                result.getRefreshToken(),
                REFRESH_TOKEN_TTL_SECONDS
        );

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(result.getAccessToken());

        return ResponseUtils.success(
                SuccessCode.LOGIN_SUCCESS,
                authResponse
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request,
                                       HttpServletResponse response,
                                       @RequestHeader(value = "Authorization",
                                               required = false)
                                           String authorizationHeader) {
        String accessToken = null;
        if (authorizationHeader != null &&
                authorizationHeader.startsWith("Bearer ")) {

            accessToken =
                    authorizationHeader.substring(7);
        }
        String refreshToken =
                cookieUtils.extractRefreshTokenFromCookies(request);

        authService.logout(accessToken, refreshToken);

        cookieUtils.clearRefreshTokenCookie(response);

        return ResponseUtils.success(
                SuccessCode.LOGOUT_SUCCESS,
                null
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken =
                cookieUtils.extractRefreshTokenFromCookies(request);

        AuthResponse result =
                authService.refresh(refreshToken);
        cookieUtils.addRefreshTokenCookie(
                response, result.getRefreshToken(), REFRESH_TOKEN_TTL_SECONDS
        );
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(result.getAccessToken());
        authResponse.setRefreshToken(result.getRefreshToken());
        authResponse.setUser(result.getUser());
        return ResponseUtils.success(
                SuccessCode.REFRESH_TOKEN_SUCCESS,
                authResponse
        );
    }

    @PutMapping("/update-password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@RequestParam Long userId, @RequestBody @Valid PasswordDTO passwordDTO) {
        authService.updatePassword(userId, passwordDTO);
        return ResponseUtils.success(
                SuccessCode.PASSWORD_UPDATED,
                null
        );
    }
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(
            @RequestBody @Valid VerifyOtpDTO request
    ) {
        authService.verifyOtp(request);
        return ResponseUtils.success(
                SuccessCode.OTP_VERIFIED,
                null
        );
    }

    @PostMapping("/resend-verify-otp")
    public ResponseEntity<ApiResponse<Void>> resendVerifyOtp(
            @RequestBody ResendOtpDTO request
    ) {
        authService.resendOtp(request.getEmail());

        return ResponseUtils.success(SuccessCode.OTP_SENT_SUCCESS, null);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @RequestBody @Valid ForgotPasswordDTO request
    ) {
        authService.forgotPassword(request);
        return ResponseUtils.success(
                SuccessCode.EMAIL_SENT_SUCCESS,
                null
        );
    }
    @PostMapping("/resend-forgot-password-otp")
    public ResponseEntity<ApiResponse<Void>> resendForgotPasswordOtp(
            @RequestBody ResendOtpDTO request
    ) {
        authService.resendForgotOtp(request.getEmail());

        return ResponseUtils.success(SuccessCode.OTP_SENT_SUCCESS, null);
    }

    @PostMapping("/confirm-reset-password")
    public ResponseEntity<ApiResponse<Void>> confirmResetPassword(
            @RequestBody @Valid VerifyOtpDTO request
    ) {
        authService.confirmResetPassword(request);
        return ResponseUtils.success(
                SuccessCode.CONFIRM_RESET_PASSWORD_SUCCESS,
                null
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestBody @Valid ResetPasswordDTO request
    ) {
        authService.resetPassword(request);
        return ResponseUtils.success(
                SuccessCode.PASSWORD_RESET_SUCCESS,
                null
        );
    }

}
