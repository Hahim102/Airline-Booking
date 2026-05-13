package com.example.controller;


import com.example.payload.dto.*;
import com.example.payload.response.AuthResponse;
import com.example.payload.response.RecaptchaResponse;
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
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid UserDTO userDTO, HttpServletResponse response) throws Exception {
        AuthResponse result = authService.register(userDTO);

        RecaptchaResponse captchaResponse = recaptchaService.verify(userDTO.getCaptchaToken());
        if (captchaResponse == null || !captchaResponse.isSuccess()) {
            String errorMessage =
                    captchaResponse != null
                            && captchaResponse.getErrorCodes() != null
                            ? captchaResponse.getErrorCodes().toString()
                            : "Captcha verification failed";

            return ResponseEntity
                    .badRequest()
                    .body(new AuthResponse(null, null, errorMessage, null, null));
        }
        AuthResponse authResponse =
                new AuthResponse();
        authResponse.setRefreshToken(null);

        authResponse.setUser(result.getUser());

        authResponse.setTitle(result.getTitle());

        authResponse.setMessage(result.getMessage());
        return ResponseEntity.ok(authResponse);
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequestDTO request, HttpServletResponse response) throws Exception {
        RecaptchaResponse captchaResponse = recaptchaService.verify(request.getCaptchaToken());

        AuthResponse result = authService.login(request.getEmail(), request.getPassword());

        cookieUtils.addRefreshTokenCookie(
                response,
                result.getRefreshToken(),
                REFRESH_TOKEN_TTL_SECONDS
        );

        if (captchaResponse == null || !captchaResponse.isSuccess()) {
            String errorMessage =
                    captchaResponse != null
                            && captchaResponse.getErrorCodes() != null
                            ? captchaResponse.getErrorCodes().toString()
                            : "Captcha verification failed";

            return ResponseEntity
                    .badRequest()
                    .body(new AuthResponse(null, null, errorMessage, null, null));
        }
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(result.getAccessToken());
        authResponse.setRefreshToken(null);
        authResponse.setUser(result.getUser());
        authResponse.setTitle(result.getTitle());
        authResponse.setMessage(result.getMessage());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,
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

        return ResponseEntity.ok().build();
    }
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String refreshToken =
                cookieUtils.extractRefreshTokenFromCookies(request);

        System.out.println(
                "Refresh token: " + refreshToken
        );
        AuthResponse result =
                authService.refresh(refreshToken);
        cookieUtils.addRefreshTokenCookie(
                response, result.getRefreshToken(), REFRESH_TOKEN_TTL_SECONDS
        );
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(result.getAccessToken());
        authResponse.setUser(result.getUser());
        return ResponseEntity.ok(authResponse);
    }

    @PutMapping("/update-profile")
    public ResponseEntity<AuthResponse> updateProfile(@RequestParam Long userId, @RequestBody @Valid UserDTO userDTO) throws Exception {
        AuthResponse authResponse = authService.updateProfile(userId, userDTO);
        return ResponseEntity.ok(authResponse);
    }

    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestParam Long userId, @RequestBody @Valid PasswordDTO passwordDTO) throws Exception {
        authService.updatePassword(userId, passwordDTO);
        return ResponseEntity.ok("Password updated successfully");
    }
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(
            @RequestBody VerifyOtpDTO request
    ) {
        return ResponseEntity.ok(
                authService.verifyOtp(request)
        );
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(
            @RequestBody ForgotPasswordDTO request
    ) {
        return ResponseEntity.ok(
                authService.forgotPassword(request)
        );
    }

    @PostMapping("/confirm-reset-password")
    public ResponseEntity<AuthResponse> confirmResetPassword(
            @RequestBody VerifyOtpDTO request
    ) {
        return ResponseEntity.ok(
                authService.confirmResetPassword(request)
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(
            @RequestBody ResetPasswordDTO request
    ) {
        return ResponseEntity.ok(
                authService.resetPassword(request)
        );
    }

}
