package com.example.service.impl;

import com.example.enums.ErrorCode;
import com.example.exception.AppException;
import com.example.model.Users;
import com.example.payload.response.UserResponse;
import com.example.repository.UserRepository;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class UserAbstract implements UserService {
    private final UserRepository userRepository;
    public UserResponse getUserById(Long id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }
    public void verifyEmail(String email) {
        Users user = userRepository.findByEmail(email);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
    }
}
