package com.example.service;


import com.example.payload.dto.CreateUserByAdminDTO;
import com.example.payload.dto.UpdateUserProfileDTO;
import com.example.payload.dto.UserSearchFilterDTO;
import com.example.payload.response.CreateUserResponse;
import com.example.payload.response.UserResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserResponse getUserByEmail(String email) throws Exception;
    UserResponse getUserById(Long id) throws Exception;
    List<UserResponse> getAllUsers();
    void updateIsActiveStatus(Long userId, boolean isActive) throws Exception;
    void deleteUser(Long userId) throws Exception;

    CreateUserResponse createUser(CreateUserByAdminDTO request) throws Exception;

    UserResponse updateUserProfile(Long userId, UpdateUserProfileDTO updateRequest) throws Exception;

    Page<UserResponse> searchAndFilterUsers(UserSearchFilterDTO searchFilter);

    List<Map<String, String>> getUsersContactInfo();
}

