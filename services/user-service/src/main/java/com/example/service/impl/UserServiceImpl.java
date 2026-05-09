package com.example.service.impl;

import com.example.model.User;
import com.example.payload.request.CreateUserRequest;
import com.example.payload.response.CreateUserResponse;
import com.example.payload.response.UserResponse;
import com.example.repository.UserRepository;
import com.example.service.UserService;
import com.example.util.ModelMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private static final String USER_CACHE = "USER_CACHE";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Cacheable(value = USER_CACHE, key = "'EMAIL:' + #email")
    @Override
    public UserResponse getUserByEmail(String email) throws Exception {

        log.info("Getting user by email call db: " + email);

        User user = userRepository.findByEmailAndDeletedIsFalse(email);

        if(user == null) {
            throw new Exception("User not found with email: " + email);
        }
        return ModelMapperUtil.mapper(user, UserResponse.class);
    }
    @Cacheable(value = USER_CACHE, key = "'ID:' + #id")
    @Override
    public UserResponse getUserById(Long id) throws Exception {
        User user = userRepository.findByIdAndDeletedIsFalse(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ModelMapperUtil.mapper(user, UserResponse.class);
    }

    @Cacheable(value = USER_CACHE, key = "'ALL'")
    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAllByDeletedIsFalse();
        return ModelMapperUtil.mapList(users, UserResponse.class);
    }

    @CacheEvict(value = USER_CACHE, allEntries = true)
    @Override
    public UserResponse updateIsActiveStatus(Long userId, boolean isActive) throws Exception {
        User user = userRepository.findByIdAndDeletedIsFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(isActive);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        return ModelMapperUtil.mapper(user, UserResponse.class);
    }

    @CacheEvict(value = USER_CACHE, allEntries = true)
    @Override
    public void deleteUser(Long userId) throws Exception {
        User user = userRepository.findByIdAndDeletedIsFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setDeleted(true);
        user.setActive(false);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @CacheEvict(value = USER_CACHE, allEntries = true)
    @Override
    public CreateUserResponse createUser(CreateUserRequest request) throws Exception {
        User existingUser = userRepository.findByEmailAndDeletedIsFalse(request.getEmail());
        if (existingUser != null) {
            throw new Exception("Email already exists");
        }

        String rawPassword = request.getPassword();

        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(rawPassword))
                .phone(request.getPhone())
                .role(request.getRole())
                .active(true)
                .deleted(false)
                .fullName(request.getFullName())
                .createdAt(LocalDateTime.now())
                .build();
        User savedUser = userRepository.save(newUser);

        CreateUserResponse response = ModelMapperUtil.mapper(savedUser, CreateUserResponse.class);

        response.setPassword(rawPassword);
        return response;
    }


}
