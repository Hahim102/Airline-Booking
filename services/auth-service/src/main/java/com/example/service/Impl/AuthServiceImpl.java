package com.example.service.Impl;

import com.example.config.JwtProvider;
import com.example.enums.UserRole;
import com.example.jwt.JwtUtils;
import com.example.model.User;
import com.example.payload.dto.PasswordDTO;
import com.example.payload.dto.UserDTO;
import com.example.payload.response.AuthResponse;
import com.example.payload.response.UserResponse;
import com.example.repository.UserRepository;
import com.example.service.AuthService;
import com.example.service.RedisTokenService;
import com.example.service.UserDetailService;
import com.example.util.CookieUtils;
import com.example.util.ModelMapperUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;


@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final int REFRESH_TOKEN_TTL_SECONDS = 7 * 24 * 60 * 60;
    private static final long REFRESH_TOKEN_TTL_MILLISECONDS = 7 * 24 * 60 * 60 * 1000L;
    private static final long ACCESS_TOKEN_TTL_MILLISECONDS = 15 * 60 * 1000L; // 15 minutes
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailService userDetailService;
    private final RedisTokenService redisTokenService;
    private final CookieUtils cookieUtils;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    /*
    1. Check if email exists in the database
    2. Encode password using BCryptPasswordEncoder
    3. Save user to the database
    4. Generate JWT token
    5. Save refresh token to Redis
    6. Return AuthResponse with token and user details
    */
    @Override
    public AuthResponse register(UserDTO request, HttpServletResponse response) throws Exception {
        User existingUser = userRepository.findByEmailAndDeletedIsFalse(request.getEmail());
        if (existingUser != null) {
            throw new Exception("Email already exists");
        }
        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(UserRole.ROLE_USER)
                .active(true)
                .deleted(false)
                .fullName(request.getFullName())
                .createdAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .build();
        User savedUser = userRepository.save(newUser);

        UserResponse userResponse = ModelMapperUtil.mapper(savedUser, UserResponse.class);

        UserDetails userDetails = userDetailService.loadUserByUsername(savedUser.getEmail());

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        String accessToken = jwtProvider.generateAccessToken(authentication, savedUser.getId());
        String refreshToken = jwtProvider.generateRefreshToken(savedUser.getId());

        redisTokenService.saveRefreshToken(savedUser.getId(), refreshToken, REFRESH_TOKEN_TTL_MILLISECONDS);

        cookieUtils.addRefreshTokenCookie(response, refreshToken, REFRESH_TOKEN_TTL_SECONDS);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(accessToken);
        authResponse.setUser(userResponse);
        authResponse.setTitle("Hello " + savedUser.getFullName());
        authResponse.setMessage("Registration successful");

        return authResponse;
    }

    /*
    1. Load user by email
    2. Compare password using BCryptPasswordEncoder
    3. Update last login time
    4. Generate JWT token
    5. Save refresh token to Redis
    6. Return AuthResponse with token and user details
    */
    @Override
    public AuthResponse login(String email, String password, HttpServletResponse response) throws Exception {
        Authentication authentication = authenticationManager.
                authenticate(new UsernamePasswordAuthenticationToken(email, password));
        User user = userRepository.findByEmailAndDeletedIsFalse(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        UserResponse userResponse = ModelMapperUtil.mapper(user, UserResponse.class);

        String accessToken = jwtProvider.generateAccessToken(authentication, user.getId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        redisTokenService.saveRefreshToken(user.getId(), refreshToken, REFRESH_TOKEN_TTL_MILLISECONDS);

        cookieUtils.addRefreshTokenCookie(response, refreshToken, REFRESH_TOKEN_TTL_SECONDS);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(accessToken);
        authResponse.setUser(userResponse);
        authResponse.setTitle("Hello " + user.getFullName());
        authResponse.setMessage("Login successful");
        return authResponse;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, String accessToken) {
        try {
            if (accessToken != null && JwtUtils.isTokenValid(accessToken)) {
                redisTokenService.blacklistAccessToken(accessToken, ACCESS_TOKEN_TTL_MILLISECONDS);
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token");
        }
        
        String refreshToken = cookieUtils.extractRefreshTokenFromCookies(request);
        if (refreshToken != null) {
            redisTokenService.revokeRefreshToken(refreshToken);
        }

        cookieUtils.clearRefreshTokenCookie(response);
    }

    @Override
    public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtils.extractRefreshTokenFromCookies(request);
        if (refreshToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is missing");
        }

        if (!redisTokenService.isRefreshTokenValid(refreshToken)) {
            cookieUtils.clearRefreshTokenCookie(response);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }

        Long userId = redisTokenService.getUserIdFromRefreshToken(refreshToken);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        User user = userRepository.findById(userId)
                .filter(u -> u.isActive() && !u.isDeleted())
                .orElseThrow(() -> new RuntimeException("User is inactive or deleted"));

        redisTokenService.revokeRefreshToken(refreshToken);

        String newRefreshToken = jwtProvider.generateRefreshToken(user.getId());
        
        redisTokenService.saveRefreshToken(user.getId(), newRefreshToken, REFRESH_TOKEN_TTL_MILLISECONDS);

        cookieUtils.addRefreshTokenCookie(response, newRefreshToken, REFRESH_TOKEN_TTL_SECONDS);

        UserDetails userDetails = userDetailService.loadUserByUsername(user.getEmail());

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        String newAccessToken = jwtProvider.generateAccessToken(authentication, user.getId());

        AuthResponse res = new AuthResponse();
        res.setToken(newAccessToken);
        res.setUser(ModelMapperUtil.mapper(user, UserResponse.class));

        return res;
    }

    @Override
    public void updatePassword(Long userId, PasswordDTO passwordDTO) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found with id: " + userId));
        if (!passwordEncoder.matches(passwordDTO.getCurrentPassword(), user.getPassword())) {
            throw new Exception("Wrong password");
        }

        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public AuthResponse updateProfile(Long userId, UserDTO userDTO) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow();

        User existing = userRepository.findByEmailAndDeletedIsFalse(userDTO.getEmail());
        if (existing != null && !existing.getId().equals(userId)) {
            throw new RuntimeException("Email already in use");
        }

        user.setFullName(userDTO.getFullName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);

        UserResponse userResponse = ModelMapperUtil.mapper(updatedUser, UserResponse.class);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setUser(userResponse);
        authResponse.setMessage("Profile updated successfully");
        authResponse.setTitle("Profile Update");

        return authResponse;
    }
}



