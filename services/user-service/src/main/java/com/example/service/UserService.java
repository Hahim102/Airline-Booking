package com.example.service;


import com.example.enums.StatisticType;
import com.example.payload.dto.CreateUserByAdminDTO;
import com.example.payload.dto.UpdateUserProfileDTO;
import com.example.payload.dto.UserSearchFilterDTO;
import com.example.payload.response.CreateUserResponse;
import com.example.payload.response.UserRegistrationStatsResponse;
import com.example.payload.response.UserResponse;
import com.example.payload.response.UserSummaryResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserResponse getUserByEmail(String email);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    void updateIsActiveStatus(Long userId, boolean isActive);
    void deleteUser(Long userId);

    CreateUserResponse createUser(CreateUserByAdminDTO request);

    UserResponse updateUserProfile(Long userId, UpdateUserProfileDTO updateRequest);

    Page<UserResponse> searchAndFilterUsers(UserSearchFilterDTO searchFilter);

    List<Map<String, String>> getUsersContactInfo();

    UserSummaryResponse getUserSummary();
    List<UserRegistrationStatsResponse> getUserRegistrationStats(StatisticType type);
}

