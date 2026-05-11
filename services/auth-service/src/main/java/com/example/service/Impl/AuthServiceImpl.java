package com.example.service.Impl;

import com.example.config.JwtProvider;
import com.example.enums.UserRole;
import com.example.jwt.JwtUtils;
import com.example.model.Users;
import com.example.payload.dto.PasswordDTO;
import com.example.payload.dto.UserDTO;
import com.example.payload.response.AuthResponse;
import com.example.payload.response.UserResponse;
import com.example.repository.BlacklistedTokenRepository;
import com.example.repository.RefreshTokenRepository;
import com.example.repository.UserRepository;
import com.example.service.AuthService;
import com.example.service.RedisTokenService;
import com.example.service.UserDetailService;
import com.example.util.CookieUtils;
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
    private final CookieUtils cookieUtils;
    private final RedisTokenService redisTokenService;


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
                .active(true)
                .deleted(false)
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

//        refreshTokenRepository.save(
//                new RefreshToken(null, refreshToken, savedUsers.getId(),
//                        new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))
//        );

        redisTokenService.saveRefreshToken(
                savedUsers.getId(),
                refreshToken,
                jwtProvider.getRefreshExpiration()
        );


        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshToken);
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
        System.out.println(
                redisTokenService.getRefreshToken(users.getId())
        );


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
}



