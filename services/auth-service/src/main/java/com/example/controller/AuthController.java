package com.example.controller;


import com.example.payload.dto.UserDTO;
import com.example.payload.request.LoginRequest;
import com.example.payload.response.AuthResponse;
import com.example.service.AuthService;
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

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid UserDTO userDTO, HttpServletResponse response) throws Exception {
        AuthResponse authResponse = authService.register(userDTO, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request, HttpServletResponse response) throws Exception {
        AuthResponse authResponse = authService.login(request.getEmail(), request.getPassword(), response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response, @RequestHeader("Authorization") String token) {
        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        authService.logout(request, response, jwtToken);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) throws Exception {
        AuthResponse authResponse = authService.refresh(request, response);
        return ResponseEntity.ok(authResponse);
    }
}
