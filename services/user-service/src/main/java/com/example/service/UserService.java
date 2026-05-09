package com.example.service;


import com.example.payload.dto.UserDTO;
import com.example.payload.request.CreateUserRequest;
import com.example.payload.response.CreateUserResponse;
import com.example.payload.response.UserResponse;
import org.springframework.security.core.userdetails.User;

import java.util.List;

public interface UserService {
    UserResponse getUserByEmail(String email) throws Exception;
    UserResponse getUserById(Long id) throws Exception;
    List<UserResponse> getAllUsers();
    UserResponse updateIsActiveStatus(Long userId, boolean isActive) throws Exception;
    void deleteUser(Long userId) throws Exception;

    CreateUserResponse createUser(CreateUserRequest request) throws Exception;
}
