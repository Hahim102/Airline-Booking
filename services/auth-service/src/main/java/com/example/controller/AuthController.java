package com.example.controller;


import com.example.payload.dto.PasswordDTO;
import com.example.payload.dto.UserDTO;
import com.example.payload.request.LoginRequest;
import com.example.payload.response.AuthResponse;
import com.example.payload.response.RecaptchaResponse;
import com.example.service.AuthService;
import com.example.service.Impl.RecaptchaService;
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

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid UserDTO userDTO, HttpServletResponse response) throws Exception {
        RecaptchaResponse captchaResponse = recaptchaService.verify(userDTO.getCaptchaToken());
        if (captchaResponse == null || !captchaResponse.isSuccess()) {
            String errorMessage =
                    captchaResponse != null
                            && captchaResponse.getErrorCodes() != null
                            ? captchaResponse.getErrorCodes().toString()
                            : "Captcha verification failed";

            return ResponseEntity
                    .badRequest()
                    .body(new AuthResponse(null, errorMessage, null, null));
        }
        AuthResponse authResponse = authService.register(userDTO, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request, HttpServletResponse response) throws Exception {
        RecaptchaResponse captchaResponse = recaptchaService.verify(request.getCaptchaToken());
        if (captchaResponse == null || !captchaResponse.isSuccess()) {
            String errorMessage =
                    captchaResponse != null
                            && captchaResponse.getErrorCodes() != null
                            ? captchaResponse.getErrorCodes().toString()
                            : "Captcha verification failed";

            return ResponseEntity
                    .badRequest()
                    .body(new AuthResponse(null, errorMessage, null, null));
        }
        AuthResponse authResponse = authService.login(request.getEmail(), request.getPassword(), response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        String jwtToken = token != null && token.startsWith("Bearer ") ? token.substring(7) : token;
        authService.logout(request, response, jwtToken);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) throws Exception {
        AuthResponse authResponse = authService.refresh(request, response);
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
}
