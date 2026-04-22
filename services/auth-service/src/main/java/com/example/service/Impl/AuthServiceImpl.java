package com.example.service.Impl;

import com.example.enums.UserRole;
import com.example.model.User;
import com.example.payload.dto.UserDTO;
import com.example.payload.response.AuthResponse;
import com.example.repository.UserRepository;
import com.example.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;

    /*
    1. Check if email exists in the database
    2. Encode password using BCryptPasswordEncoder
    3. Save user to the database
    4. Generate JWT token
    5. Return AuthResponse with token and user details
    */


    @Override
    public AuthResponse login(String email, String password) {

        return null;
    }

    @Override
    public AuthResponse register(UserDTO request) throws Exception {
        User existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser != null) {
            throw new Exception("Email already exists");
        }
        if (request.getRole() == UserRole.ROLE_SYSTEM_ADMIN) {
            throw new Exception("Role system admin is not allowed");
        }
        User newUser = User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .phone(request.getPhone())
                .role(request.getRole())
                .fullName(request.getFullName())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .build();
    }
}
