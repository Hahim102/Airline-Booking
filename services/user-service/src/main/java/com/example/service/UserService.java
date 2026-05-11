package com.example.service;


import com.example.payload.dto.CreateUserByAdminDTO;
import com.example.payload.response.CreateUserResponse;
import com.example.payload.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse getUserByEmail(String email) throws Exception;
    UserResponse getUserById(Long id) throws Exception;
    List<UserResponse> getAllUsers();
    void updateIsActiveStatus(Long userId, boolean isActive) throws Exception;
    void deleteUser(Long userId) throws Exception;

    CreateUserResponse createUser(CreateUserByAdminDTO request) throws Exception;
}
