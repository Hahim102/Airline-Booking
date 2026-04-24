package com.example.service;


import com.example.payload.dto.UserDTO;
import com.example.payload.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse getUserByEmail(String email) throws Exception;
    UserResponse getUserById(Long id) throws Exception;
    List<UserResponse> getAllUsers();
}
