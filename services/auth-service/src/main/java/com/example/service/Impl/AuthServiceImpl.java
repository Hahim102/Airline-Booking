package com.example.service.Impl;

import com.example.config.JwtProvider;
import com.example.enums.ErrorCode;
import com.example.enums.UserRole;
import com.example.event.EmailEvent;
import com.example.exception.AppException;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;


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
    private final Logger logger = Logger.getLogger(AuthServiceImpl.class.getName());


    /*
    1. Check if email exists in the database
    2. Encode password using BCryptPasswordEncoder
    3. Save user to the database
    4. Generate JWT token
    5. Return AuthResponse with token and user details
    */


    @Transactional
    @Override
    public void register(UserDTO request) {
        boolean existsUser = userRepository.existsByEmail(request.getEmail());
        if (existsUser) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        Users savedUser;
        try {
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
                    .build();
            savedUser = userRepository.save(newUsers);

        } catch (Exception e) {
            throw new AppException(ErrorCode.USER_CREATE_FAILED);
        }
        String otp = generateOtp();

        redisOtpService.saveVerifyOtp(savedUser.getEmail(), otp);

        try {
            redisOtpService.saveVerifyOtp(savedUser.getEmail(), otp);
        } catch (Exception e) {
            throw new AppException(ErrorCode.OTP_VERIFY_FAILED);
        }

        logger.info("OTP generated: " + otp);

        EmailEvent event = new EmailEvent(
                savedUser.getEmail(),
                "Verify your Airline Booking account",
                "VERIFY_OTP",
                Map.of(
                        "name", savedUser.getFullName(),
                        "otp", otp
                )
        );

        try {
            kafkaProducerService.sendEmailEvent(event);
        } catch (Exception e) {
            throw new AppException(ErrorCode.KAFKA_PUBLISH_FAILED);
        }

    }

    @Override
    public void resendOtp(String email) {
        Users user = userRepository.findByEmail(email);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.isActive()) {
            throw new AppException(ErrorCode.USER_ALREADY_VERIFIED);
        }

        String otp = generateOtp();

        redisOtpService.saveVerifyOtp(email, otp);

        try {
            redisOtpService.saveVerifyOtp(email, otp);
        } catch (Exception e) {
            throw new AppException(ErrorCode.OTP_VERIFY_FAILED);
        }

        logger.info("OTP generated: " + otp);

        EmailEvent event = new EmailEvent(
                email,
                "Verify your Airline Booking account",
                "VERIFY_OTP",
                Map.of(
                        "name", email,
                        "otp", otp
                )
        );

        try {
            kafkaProducerService.sendEmailEvent(event);
        } catch (Exception e) {
            throw new AppException(ErrorCode.KAFKA_PUBLISH_FAILED);
        }

    }

    /*
    1. Load user by email
    2. Compare password using BCryptPasswordEncoder
    3. Update last login time
    4. Generate JWT token
    5. Return AuthResponse with token and user details
    */


    @Override
    public AuthResponse login(String email, String password) {
        Users users = userRepository
                .findByEmailAndDeletedIsFalseAndActiveIsTrue(email)
                .orElseThrow(() ->
                        new AppException(ErrorCode.AUTHENTICATION_FAILED));

        if (!users.isActive()) {
            throw new AppException(ErrorCode.USER_NOT_VERIFIED);
        }

        Authentication authentication = authenticationManager.
                authenticate(new UsernamePasswordAuthenticationToken(email, password));


        users.setLastLoginAt(LocalDateTime.now());
        userRepository.save(users);

        UserResponse userResponse = ModelMapperUtil.mapper(users, UserResponse.class);

        String accessToken = jwtProvider.generateAccessToken(authentication, users.getId());
        String refreshToken = jwtProvider.generateRefreshToken(users.getId());


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
            //
        }

        try {
            if (refreshToken != null && JwtUtils.isTokenValid(refreshToken)) {
                Long userId = JwtUtils.extractUserId(refreshToken);
                redisTokenService.deleteRefreshToken(userId);
            }
        } catch (Exception e) {
            //
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
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId;

        try {
            if (!JwtUtils.isTokenValid(refreshToken)) {
                throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
            }

            Date expiration = JwtUtils.extractAllClaims(refreshToken).getExpiration();

            if (expiration.before(new Date())) {
                throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
            }

            userId = JwtUtils.extractUserId(refreshToken);

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }


        String redisToken =
                redisTokenService.getRefreshToken(userId);


        if (redisToken == null ||
                !redisToken.equals(refreshToken)) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Users user = userRepository.findById(userId)
                .filter(u -> u.isActive() && !u.isDeleted())
                .orElseThrow(() ->
                        new AppException(ErrorCode.USER_DISABLED));

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
    public void updatePassword(Long userId, PasswordDTO passwordDTO) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(passwordDTO.getCurrentPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.OLD_PASSWORD_INCORRECT);
        }

        if (passwordEncoder.matches(passwordDTO.getNewPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.NEW_PASSWORD_DUPLICATE);
        }


        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public AuthResponse updateProfile(Long id, UserDTO userDTO) {
        Users user = userRepository.findByIdAndDeletedIsFalseAndActiveIsTrue(id)
                .orElseThrow(() ->
                        new AppException(ErrorCode.USER_NOT_FOUND));

        if (!user.getEmail().equals(userDTO.getEmail())
                && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_USED);
        }

        user.setFullName(userDTO.getFullName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setUpdatedAt(LocalDateTime.now());

        Users updatedUser = userRepository.save(user);

        UserResponse userResponse = ModelMapperUtil.mapper(updatedUser, UserResponse.class);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setUser(userResponse);
        return authResponse;
    }
    @Transactional
    public void verifyOtp(VerifyOtpDTO request) {

        String redisOtp = redisOtpService.getVerifyOtp(request.getEmail());

        if (redisOtp == null) {
            throw new AppException(ErrorCode.OTP_NOT_FOUND);
        }

        if (!redisOtp.equals(request.getOtp())) {
            throw new AppException(ErrorCode.OTP_INVALID);
        }

        Users user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        if (user.isActive()) {
            throw new AppException(ErrorCode.USER_ALREADY_VERIFIED);
        }

        user.setActive(true);

        userRepository.save(user);

        redisOtpService.deleteVerifyOtp(request.getEmail());
    }

    @Override
    public void forgotPassword(ForgotPasswordDTO request) {
        Users user = userRepository
                .findByEmailAndDeletedIsFalseAndActiveIsTrue(
                        request.getEmail())
                .orElseThrow(() ->
                        new AppException(ErrorCode.USER_NOT_FOUND));

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
            throw new AppException(ErrorCode.KAFKA_PUBLISH_FAILED);
        }
    }

    @Override
    public void confirmResetPassword(VerifyOtpDTO request) {
        String redisOtp = redisOtpService.getResetPasswordOtp(request.getEmail());

        if (redisOtp == null) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        if (!redisOtp.equals(request.getOtp())) {
            throw new AppException(ErrorCode.OTP_INVALID);
        }

        Users user = userRepository.findByEmailAndDeletedIsFalseAndActiveIsTrue(request.getEmail())
                .orElseThrow(() ->
                        new AppException(ErrorCode.USER_NOT_FOUND));

        redisOtpService.markResetPasswordOtpAsVerified(request.getEmail());
    }

    @Override
    public void resetPassword(ResetPasswordDTO request) {
        String redisOtp = redisOtpService.getResetPasswordOtp(
                request.getEmail()
        );

        if (redisOtp == null) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        if (!redisOtp.equals(request.getOtp())) {
            throw new AppException(ErrorCode.OTP_INVALID);
        }


        if (!redisOtpService.isResetPasswordOtpVerified(request.getEmail())) {
            throw new AppException(ErrorCode.OTP_VERIFY_FAILED);
        }

        Users user = userRepository.findByEmailAndDeletedIsFalseAndActiveIsTrue(
                request.getEmail())
                .orElseThrow(() ->
                        new AppException(ErrorCode.USER_NOT_FOUND));


        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        redisOtpService.deleteResetPasswordOtp(request.getEmail());

    }

    private String generateOtp() {
        return String.valueOf(
                ThreadLocalRandom.current().nextInt(100000, 1000000)
        );
    }
}



