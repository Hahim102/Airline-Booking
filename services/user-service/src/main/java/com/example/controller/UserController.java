package com.example.controller;


import com.example.config.UserPrincipal;
import com.example.enums.StatisticType;
import com.example.enums.SuccessCode;
import com.example.payload.dto.CreateUserByAdminDTO;
import com.example.payload.dto.UpdateUserProfileDTO;
import com.example.payload.dto.UserDTO;
import com.example.payload.dto.UserSearchFilterDTO;
import com.example.payload.response.*;
import com.example.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "minio.enabled",
        havingValue = "true"
)
public class UserController {
    public final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(@RequestHeader("X-User-Email") String email) {
            UserResponse user = userService.getUserByEmail(email);
            return ResponseUtils.success(SuccessCode.SUCCESS, user);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMypProfile(@RequestHeader("X-User-Email") String email) {
        UserResponse user = userService.getUserByEmail(email);
        return ResponseUtils.success(SuccessCode.SUCCESS, user);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseUtils.success(SuccessCode.SUCCESS, user);
    }

    @GetMapping()
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseUtils.success(
                SuccessCode.SUCCESS,
                users
        );
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<Void>> updateIsActive(
            @PathVariable Long userId,
            @RequestParam boolean isActive) {
        userService.updateIsActiveStatus(userId, isActive);
        return ResponseUtils.success(SuccessCode.SUCCESS, null);
    }

    @DeleteMapping("/{userId}/delete")
    public ResponseEntity<ApiResponse<Void>> deleteUserById(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseUtils.success(SuccessCode.USER_DELETED, null);
    }

    @PutMapping("/{userId}/update-user-profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            @PathVariable Long userId,
            @RequestBody UpdateUserProfileDTO updateRequest) {
        UserResponse updatedUser = userService.updateUserProfile(userId, updateRequest);
        return ResponseUtils.success(SuccessCode.PROFILE_UPDATED, updatedUser);
    }

    @PostMapping("/create-user")
    public ResponseEntity<ApiResponse<CreateUserResponse>> createUser(@RequestBody CreateUserByAdminDTO request) {
        CreateUserResponse createdUser = userService.createUser(request);
        return ResponseUtils.success(SuccessCode.USER_CREATED, createdUser);
    }

    @GetMapping("/export-data")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersForExport() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseUtils.success(SuccessCode.SUCCESS, users);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> searchAndFilterUsers(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortOrder) {

        UserSearchFilterDTO searchFilter = new UserSearchFilterDTO();
        searchFilter.setFullName(fullName);
        searchFilter.setEmail(email);
        searchFilter.setPhone(phone);

        if (role != null && !role.isEmpty() && !role.equalsIgnoreCase("ALL_ROLES")) {
            try {
                searchFilter.setRole(com.example.enums.UserRole.valueOf(role.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role value: " + role);
            }
        }
        
        searchFilter.setIsActive(isActive);
        searchFilter.setPageNumber(pageNumber);
        searchFilter.setPageSize(pageSize);
        searchFilter.setSortBy(sortBy);
        searchFilter.setSortOrder(sortOrder);

        Page<UserResponse> results = userService.searchAndFilterUsers(searchFilter);
        return ResponseUtils.success(
                SuccessCode.SUCCESS,
                results
        );
    }


    @GetMapping("/contact-info")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getUsersContactInfo() {
        List<Map<String, String>> contactInfo = userService.getUsersContactInfo();
        return ResponseUtils.success(SuccessCode.SUCCESS, contactInfo);
    }

    @GetMapping("/statistics/summary")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> getUserSummary() {
        UserSummaryResponse userSummary = userService.getUserSummary();
        return ResponseUtils.success(SuccessCode.SUCCESS, userSummary);
    }

    @GetMapping("/statistics/registrations")
    public ResponseEntity<ApiResponse<List<UserRegistrationStatsResponse>>> getUserRegistrationStats(
            @RequestParam(defaultValue = "DAY") StatisticType type
    ) {
        List<UserRegistrationStatsResponse> userRegistrationStats = userService.getUserRegistrationStats(type);
        return ResponseUtils.success(SuccessCode.SUCCESS, userRegistrationStats);
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserAvatarResponse>> uploadAvatar(
            @AuthenticationPrincipal
            UserPrincipal principal,
            @RequestParam("file") MultipartFile file
    ) {
        UserAvatarResponse avatarResponse = userService.uploadUserAvatar(principal.getId(), file);

        return ResponseUtils.success(
                SuccessCode.SUCCESS,
                avatarResponse);
    }

    @PutMapping("/me/update-profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal
            UserPrincipal principal,
            @RequestBody @Valid UserDTO userDTO
    ) {
        UserResponse updateMyProfile = userService.updateProfile(principal.getId(), userDTO);

        return ResponseUtils.success(SuccessCode.PROFILE_UPDATED, updateMyProfile);
    }

}

