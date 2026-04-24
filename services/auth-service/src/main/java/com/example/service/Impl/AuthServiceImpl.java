package com.example.service.Impl;

import com.example.config.JwtProvider;
import com.example.enums.UserRole;
import com.example.mapper.UserMapper;
import com.example.model.User;
import com.example.payload.dto.UserDTO;
import com.example.payload.response.AuthResponse;
import com.example.repository.UserRepository;
import com.example.service.AuthService;
import com.example.service.UserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailService userDetailService;

    /*
    1. Check if email exists in the database
    2. Encode password using BCryptPasswordEncoder
    3. Save user to the database
    4. Generate JWT token
    5. Return AuthResponse with token and user details
    */


    @Override
    public AuthResponse register(UserDTO request) throws Exception {
        User existingUsers = userRepository.findByEmail(request.getEmail());
        if (existingUsers != null) {
            throw new Exception("Email already exists");
        }
        if (request.getRole() == UserRole.ROLE_SYSTEM_ADMIN) {
            throw new Exception("Role system admin is not allowed");
        }
        User newUsers = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .fullName(request.getFullName())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .build();
        User savedUsers = userRepository.save(newUsers);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                savedUsers.getEmail(), savedUsers.getPassword()
        );

        String jwt = new JwtProvider().generateToken(authentication, savedUsers.getId());


        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(jwt);
        authResponse.setUser(UserMapper.toDTO(savedUsers));
        authResponse.setTitle("Hello " + savedUsers.getFullName());
        authResponse.setMessage("Registration successful");

        return authResponse;
    }

    /*
    1. Load user by email
    2. Compare password using BCryptPasswordEncoder
    3. Update last login time
    4. Generate JWT token
    5. Return AuthResponse with token and user details
    */

    @Override
    public AuthResponse login(String email, String password) throws Exception {
        Authentication authentication = authenticate(email, password);
        User users = userRepository.findByEmail(email);
        users.setLastLoginAt(LocalDateTime.now());
        userRepository.save(users);

        String jwt = new JwtProvider().generateToken(authentication, users.getId());
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(jwt);
        authResponse.setUser(UserMapper.toDTO(users));
        authResponse.setTitle("Hello " + users.getFullName());
        authResponse.setMessage("Login successful");
        return authResponse;
    }

    private Authentication authenticate(String email, String password) throws Exception {
        UserDetails userDetails = userDetailService.loadUserByUsername(email);

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new Exception("Invalid password");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
