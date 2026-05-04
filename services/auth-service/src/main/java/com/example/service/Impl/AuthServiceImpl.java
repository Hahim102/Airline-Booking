package com.example.service.Impl;

import com.example.config.JwtProvider;
import com.example.enums.UserRole;
import com.example.jwt.JwtUtils;
import com.example.model.BlacklistedToken;
import com.example.model.RefreshToken;
import com.example.model.Users;
import com.example.payload.dto.UserDTO;
import com.example.payload.response.AuthResponse;
import com.example.payload.response.UserResponse;
import com.example.repository.BlacklistedTokenRepository;
import com.example.repository.RefreshTokenRepository;
import com.example.repository.UserRepository;
import com.example.service.AuthService;
import com.example.service.UserDetailService;
import com.example.util.ModelMapperUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;


@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailService userDetailService;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    /*
    1. Check if email exists in the database
    2. Encode password using BCryptPasswordEncoder
    3. Save user to the database
    4. Generate JWT token
    5. Return AuthResponse with token and user details
    */


    @Override
    public AuthResponse register(UserDTO request, HttpServletResponse response) throws Exception {
        Users existingUsers = userRepository.findByEmail(request.getEmail());
        if (existingUsers != null) {
            throw new Exception("Email already exists");
        }
        Users newUsers = Users.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(UserRole.ROLE_USER)
                .fullName(request.getFullName())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .build();
        Users savedUsers = userRepository.save(newUsers);
        UserResponse userResponse = ModelMapperUtil.mapper(savedUsers, UserResponse.class);

        UserDetails userDetails = userDetailService.loadUserByUsername(savedUsers.getEmail());

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        String accessToken = jwtProvider.generateAccessToken(authentication, savedUsers.getId());
        String refreshToken = jwtProvider.generateRefreshToken(savedUsers.getId());

        refreshTokenRepository.save(
                new RefreshToken(null, refreshToken, savedUsers.getId(),
                        new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))
        );

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(cookie);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(accessToken);
        authResponse.setUser(userResponse);
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
    public AuthResponse login(String email, String password, HttpServletResponse response) throws Exception {
        Authentication authentication = authenticationManager.
                authenticate(new UsernamePasswordAuthenticationToken(email, password));
        Users users = userRepository.findByEmail(email);
        users.setLastLoginAt(LocalDateTime.now());
        userRepository.save(users);

        UserResponse userResponse = ModelMapperUtil.mapper(users, UserResponse.class);

        String accessToken = jwtProvider.generateAccessToken(authentication, users.getId());
        String refreshToken = jwtProvider.generateRefreshToken(users.getId());

        refreshTokenRepository.save(
                new RefreshToken(null, refreshToken, users.getId(),
                        new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))
        );

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(cookie);


        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(accessToken);
        authResponse.setUser(userResponse);
        authResponse.setTitle("Hello " + users.getFullName());
        authResponse.setMessage("Login successful");
        return authResponse;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, String accessToken) {
        try {
            if (accessToken != null && JwtUtils.isTokenValid(accessToken)) {
                BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                        .token(accessToken)
                        .expiredAt(JwtUtils.extractAllClaims(accessToken).getExpiration())
                        .build();

                blacklistedTokenRepository.save(blacklistedToken);
            }
        } catch (Exception e) {
            // ignore token invalid/expired
        }
        String refreshToken = getCookie(request);

        if (refreshToken != null) {
            refreshTokenRepository.deleteByToken(refreshToken);
        }

        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
        }
    private String getCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("refreshToken")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getCookie(request);
        if (refreshToken == null) {
            throw new RuntimeException("Refresh token is null");
        }

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (token.getExpiryDate().before(new Date())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired");
        }

        Users user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenRepository.delete(token);

        String newRefreshToken = jwtProvider.generateRefreshToken(user.getId());

        refreshTokenRepository.save(
                new RefreshToken(
                        null,
                        newRefreshToken,
                        user.getId(),
                        new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)
                )
        );

        Cookie cookie = new Cookie("refreshToken", newRefreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // dev
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(cookie);

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
}



