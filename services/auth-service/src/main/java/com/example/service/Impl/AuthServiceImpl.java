package com.example.service.Impl;

import com.example.config.JwtProvider;
import com.example.enums.UserRole;
import com.example.event.EmailEvent;
import com.example.jwt.JwtUtils;
import com.example.model.Users;
import com.example.payload.dto.*;
import com.example.payload.response.AuthResponse;
import com.example.payload.response.UserResponse;
import com.example.repository.UserRepository;
import com.example.service.AuthService;
import com.example.service.RedisOtpService;
import com.example.service.RedisTokenService;
import com.example.service.UserDetailService;
import com.example.util.ModelMapperUtil;
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
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailService userDetailService;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final RedisTokenService redisTokenService;
    private final KafkaProducerService kafkaProducerService;
    private final RedisOtpService redisOtpService;


    /*
    1. Check if email exists in the database
    2. Encode password using BCryptPasswordEncoder
    3. Save user to the database
    4. Generate JWT token
    5. Return AuthResponse with token and user details
    */


    @Override
    public AuthResponse register(UserDTO request) throws Exception {
        Users existingUsers = userRepository.findByEmailAndDeletedIsFalse(request.getEmail());
        if (existingUsers != null) {
            throw new Exception("Email already exists");
        }
        Users newUsers = Users.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(UserRole.ROLE_USER)
                .active(false)
                .deleted(false)
                .fullName(request.getFullName())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .build();
        Users savedUsers = userRepository.save(newUsers);

        String otp = generateOtp();

        redisOtpService.saveVerifyOtp(savedUsers.getEmail(), otp);

        EmailEvent event = new EmailEvent(
                savedUsers.getEmail(),
                "Verify your Airline Booking account",
                "VERIFY_OTP",
                Map.of(
                        "name", savedUsers.getFullName(),
                        "otp", otp
                )
        );

        try {
            kafkaProducerService.sendEmailEvent(event);
        } catch (Exception e) {
            System.out.println("Kafka unavailable: " + e.getMessage());
        }

        AuthResponse authResponse = new AuthResponse();
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
        Authentication authentication = authenticationManager.
                authenticate(new UsernamePasswordAuthenticationToken(email, password));
        Users users = userRepository.findByEmailAndDeletedIsFalse(email);
        if (users == null) {
            throw new RuntimeException("User not found");
        }
        users.setLastLoginAt(LocalDateTime.now());
        userRepository.save(users);

        UserResponse userResponse = ModelMapperUtil.mapper(users, UserResponse.class);

        String accessToken = jwtProvider.generateAccessToken(authentication, users.getId());
        String refreshToken = jwtProvider.generateRefreshToken(users.getId());

//        refreshTokenRepository.save(
//                new RefreshToken(null, refreshToken, users.getId(),
//                        new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))
//        );

        redisTokenService.saveRefreshToken(
                users.getId(),
                refreshToken,
                jwtProvider.getRefreshExpiration()
        );
        EmailEvent event = new EmailEvent(
                users.getEmail(),
                "Login Notification",
                "LOGIN_SUCCESS",
                Map.of(
                        "name", users.getFullName(),
                        "time", LocalDateTime.now().toString()
                )
        );

        try {
            kafkaProducerService.sendEmailEvent(event);
        } catch (Exception e) {
            System.out.println("Kafka unavailable: " + e.getMessage());
        }


        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshToken);
        authResponse.setUser(userResponse);
        authResponse.setTitle("Hello " + users.getFullName());
        authResponse.setMessage("Login successful");
        return authResponse;
    }

    /*
    @Override
    public void logout(String accessToken, String refreshToken) {
        try {
            if (accessToken != null && JwtUtils.isTokenValid(accessToken)) {
                BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                        .token(accessToken)
                        .expiredAt(JwtUtils.extractAllClaims(accessToken).getExpiration())
                        .build();

                blacklistedTokenRepository.save(blacklistedToken);
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token");
        }

        if (refreshToken != null) {
            refreshTokenRepository.deleteByToken(refreshToken);
        }

    }
     */
    @Override
    public void logout(String accessToken, String refreshToken) {

        try {

            if (accessToken != null &&
                    JwtUtils.isTokenValid(accessToken)) {

                Date expiration =
                        JwtUtils.extractAllClaims(accessToken)
                                .getExpiration();

                long remainingTime =
                        expiration.getTime() - System.currentTimeMillis();

                if (remainingTime > 0) {

                    redisTokenService.blacklistAccessToken(
                            accessToken,
                            remainingTime
                    );
                }
            }

        } catch (Exception e) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid access token"
            );
        }

        if (refreshToken != null &&
                JwtUtils.isTokenValid(refreshToken)) {

            Long userId =
                    JwtUtils.extractUserId(refreshToken);

            redisTokenService.deleteRefreshToken(userId);
        }
    }

    /*
    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null) {
            throw new RuntimeException("Refresh token is null");
        }

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (token.getExpiryDate().before(new Date())) {
            refreshTokenRepository.deleteById(token.getId());
            throw new RuntimeException("Refresh token expired");
        }

        Users user = userRepository.findById(token.getUserId())
                .filter(u -> u.isActive() && !u.isDeleted())
                .orElseThrow(() -> new RuntimeException("User is inactive or deleted"));

        refreshTokenRepository.deleteById(token.getId());


        String newRefreshToken = jwtProvider.generateRefreshToken(user.getId());

        refreshTokenRepository.save(
                new RefreshToken(
                        null,
                        newRefreshToken,
                        user.getId(),
                        new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)
                )
        );


        UserDetails userDetails = userDetailService.loadUserByUsername(user.getEmail());

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        String newAccessToken = jwtProvider.generateAccessToken(authentication, user.getId());

        AuthResponse res = new AuthResponse();
        res.setAccessToken(newAccessToken);
        res.setRefreshToken(newRefreshToken);
        res.setUser(ModelMapperUtil.mapper(user, UserResponse.class));

        return res;
    }

     */
    @Override
    public AuthResponse refresh(String refreshToken) {

        if (refreshToken == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token missing"
            );
        }

        if (!JwtUtils.isTokenValid(refreshToken)) {
            throw new RuntimeException(
                    "Invalid refresh token"
            );
        }

        Long userId =
                JwtUtils.extractUserId(refreshToken);


        String redisToken =
                redisTokenService.getRefreshToken(userId);


        if (redisToken == null ||
                !redisToken.equals(refreshToken)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token invalid"
            );
        }

        Users user = userRepository.findById(userId)
                .filter(u -> u.isActive() && !u.isDeleted())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User inactive or deleted"
                        ));

        redisTokenService.deleteRefreshToken(userId);

        String newRefreshToken =
                jwtProvider.generateRefreshToken(user.getId());

        redisTokenService.saveRefreshToken(
                user.getId(),
                newRefreshToken,
                jwtProvider.getRefreshExpiration()
        );

        UserDetails userDetails =
                userDetailService.loadUserByUsername(
                        user.getEmail()
                );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        String newAccessToken =
                jwtProvider.generateAccessToken(
                        authentication,
                        user.getId()
                );

        AuthResponse res = new AuthResponse();

        res.setAccessToken(newAccessToken);
        res.setRefreshToken(newRefreshToken);
        res.setUser(
                ModelMapperUtil.mapper(
                        user,
                        UserResponse.class
                )
        );

        return res;
    }

    @Override
    public void updatePassword(Long userId, PasswordDTO passwordDTO) throws Exception {
        Users user = userRepository.findById(userId)
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
        Users user = userRepository.findById(userId)
                .orElseThrow();

        Users existing = userRepository.findByEmailAndDeletedIsFalse(userDTO.getEmail());
        if (existing != null && !existing.getId().equals(userId)) {
            throw new RuntimeException("Email already in use");
        }

        user.setFullName(userDTO.getFullName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setUpdatedAt(LocalDateTime.now());

        Users updatedUser = userRepository.save(user);

        UserResponse userResponse = ModelMapperUtil.mapper(updatedUser, UserResponse.class);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setUser(userResponse);
        authResponse.setMessage("Profile updated successfully");
        authResponse.setTitle("Profile Update");

        return authResponse;
    }
    @Transactional
    public AuthResponse verifyOtp(VerifyOtpDTO request) {

        System.out.println("EMAIL REQUEST = " + request.getEmail());
        System.out.println("OTP REQUEST = " + request.getOtp());
        String redisOtp = redisOtpService.getVerifyOtp(request.getEmail());
        System.out.println("OTP REDIS = " + redisOtp);

        if (redisOtp == null) {
            throw new RuntimeException("OTP expired or not found");
        }

        if (!redisOtp.equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        Users user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            throw new RuntimeException("User not found with email: " + request.getEmail());
        }

        user.setActive(true);

        userRepository.save(user);

        redisOtpService.deleteVerifyOtp(request.getEmail());

        AuthResponse authResponse = new AuthResponse();
        authResponse.setMessage("OTP verified successfully");
        return authResponse;
    }

    @Override
    public AuthResponse forgotPassword(ForgotPasswordDTO request) {
        Users user = userRepository.findByEmailAndDeletedIsFalse(
                request.getEmail()
        );

        if (user == null) {
            throw new RuntimeException("Email not found");
        }

        String otp = generateOtp();

        redisOtpService.saveResetPasswordOtp(
                user.getEmail(),
                otp
        );

        EmailEvent event = new EmailEvent(
                user.getEmail(),
                "Reset your Airline Booking password",
                "RESET_PASSWORD_OTP",
                Map.of(
                        "name", user.getFullName(),
                        "otp", otp
                )
        );

        try {
            kafkaProducerService.sendEmailEvent(event);
        } catch (Exception e) {
            System.out.println("Kafka unavailable: " + e.getMessage());
        }

        AuthResponse response = new AuthResponse();
        response.setMessage("OTP has been sent to your email");
        return response;
    }

    @Override
    public AuthResponse confirmResetPassword(VerifyOtpDTO request) {
        String redisOtp = redisOtpService.getResetPasswordOtp(request.getEmail());

        if (redisOtp == null) {
            throw new RuntimeException("OTP expired or not found");
        }

        if (!redisOtp.equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        Users user = userRepository.findByEmailAndDeletedIsFalse(request.getEmail());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Mark OTP as verified - don't delete it yet
        redisOtpService.markResetPasswordOtpAsVerified(request.getEmail());

        AuthResponse response = new AuthResponse();
        response.setMessage("OTP verified successfully. You can now reset your password.");
        return response;
    }

    @Override
    public AuthResponse resetPassword(ResetPasswordDTO request) {
        String redisOtp = redisOtpService.getResetPasswordOtp(
                request.getEmail()
        );

        if (redisOtp == null) {
            throw new RuntimeException("OTP expired or not found");
        }

        if (!redisOtp.equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }


        if (!redisOtpService.isResetPasswordOtpVerified(request.getEmail())) {
            throw new RuntimeException("OTP not verified. Please verify OTP first.");
        }

        Users user = userRepository.findByEmailAndDeletedIsFalse(
                request.getEmail()
        );

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        redisOtpService.deleteResetPasswordOtp(request.getEmail());

        AuthResponse response = new AuthResponse();
        response.setMessage("Password reset successfully");
        return response;
    }

    private String generateOtp() {
        return String.valueOf(
                ThreadLocalRandom.current().nextInt(100000, 1000000)
        );
    }
}



