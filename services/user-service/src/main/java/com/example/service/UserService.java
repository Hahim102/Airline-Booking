package com.example.service;


import com.example.enums.StatisticType;
import com.example.payload.dto.CreateUserByAdminDTO;
import com.example.payload.dto.UpdateUserProfileDTO;
import com.example.payload.dto.UserDTO;
import com.example.payload.dto.UserSearchFilterDTO;
import com.example.payload.response.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserResponse getUserByEmail(String email);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    void updateIsActiveStatus(Long userId, boolean isActive);
    void deleteUser(Long userId);

    UserResponse updateUserProfile(Long userId, UpdateUserProfileDTO updateRequest);

    UserAvatarResponse uploadUserAvatar(Long userId, MultipartFile file);

    UserResponse updateProfile(Long userId, UserDTO userDTO);

    Page<UserResponse> searchAndFilterUsers(UserSearchFilterDTO searchFilter);

    List<Map<String, String>> getUsersContactInfo();

    UserSummaryResponse getUserSummary();
    List<UserRegistrationStatsResponse> getUserRegistrationStats(StatisticType type);
}

