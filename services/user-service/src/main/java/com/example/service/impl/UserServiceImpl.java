package com.example.service.impl;

import com.example.model.Users;
import com.example.payload.dto.CreateUserByAdminDTO;
import com.example.payload.response.CreateUserResponse;
import com.example.payload.response.UserResponse;
import com.example.repository.UserRepository;
import com.example.service.UserService;
import com.example.util.ModelMapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private static final String DEFAULT_PASSWORD = "Admin@123";

    @Override
    public UserResponse getUserByEmail(String email) throws Exception {
        Users users = userRepository.findByEmailAndDeletedIsFalse(email);

        if(users == null) {
            throw new Exception("User not found with email: " + email);
        }
        return ModelMapperUtil.mapper(users, UserResponse.class);
    }

    @Override
    public UserResponse getUserById(Long id) throws Exception {
        Users users = userRepository.findByIdAndDeletedIsFalse(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ModelMapperUtil.mapper(users, UserResponse.class);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<Users> users = userRepository.findAllByDeletedIsFalse();
        return ModelMapperUtil.mapList(users, UserResponse.class);
    }

    @Override
    public void updateIsActiveStatus(Long userId, boolean isActive) throws Exception {
        Users user = userRepository.findByIdAndDeletedIsFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(isActive);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) throws Exception {
        Users user = userRepository.findByIdAndDeletedIsFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setDeleted(true);
        user.setActive(false);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    @Override
    public CreateUserResponse createUser(CreateUserByAdminDTO request) throws Exception {
        Users existingUser = userRepository.findByEmailAndDeletedIsFalse(request.getEmail());
        if (existingUser != null) {
            throw new Exception("Email already exists");
        }


        Users newUser = Users.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .phone(request.getPhone())
                .role(request.getRole())
                .active(true)
                .deleted(false)
                .fullName(request.getFullName())
                .createdAt(LocalDateTime.now())
                .build();
        Users savedUser = userRepository.save(newUser);

        CreateUserResponse response = ModelMapperUtil.mapper(savedUser, CreateUserResponse.class);

        response.setPassword(DEFAULT_PASSWORD);
        return response;
    }


}
