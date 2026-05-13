package com.example.controller;


import com.example.payload.dto.CreateUserByAdminDTO;
import com.example.payload.dto.UserDTO;
import com.example.payload.dto.UpdateUserProfileDTO;
import com.example.payload.dto.UserSearchFilterDTO;
import com.example.payload.response.CreateUserResponse;
import com.example.payload.response.UserResponse;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    public final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getUserProfile(@RequestHeader("X-User-Email") String email) throws Exception {
            UserResponse user = userService.getUserByEmail(email);
            return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMypProfile(@RequestHeader("X-User-Email") String email) throws Exception {
        UserResponse user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) throws Exception {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping()
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<String> updateIsActive(
            @PathVariable Long userId,
            @RequestParam boolean isActive) throws Exception {
        userService.updateIsActiveStatus(userId, isActive);
        return ResponseEntity.ok("User active status updated successfully");
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUserById(@PathVariable Long userId) throws Exception {
        userService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUserProfile(
            @PathVariable Long userId,
            @RequestBody UpdateUserProfileDTO updateRequest) throws Exception {
        UserResponse updatedUser = userService.updateUserProfile(userId, updateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/create-user")
    public ResponseEntity<CreateUserResponse> createUser(@RequestBody CreateUserByAdminDTO request) throws Exception {
        CreateUserResponse createdUser = userService.createUser(request);
        return ResponseEntity.ok(createdUser);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserResponse>> searchAndFilterUsers(
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
                // Invalid role, ignore
            }
        }
        
        searchFilter.setIsActive(isActive);
        searchFilter.setPageNumber(pageNumber);
        searchFilter.setPageSize(pageSize);
        searchFilter.setSortBy(sortBy);
        searchFilter.setSortOrder(sortOrder);

        Page<UserResponse> results = userService.searchAndFilterUsers(searchFilter);
        return ResponseEntity.ok(results);
    }


    @GetMapping("/contact-info")
    public ResponseEntity<List<Map<String, String>>> getUsersContactInfo() {
        List<Map<String, String>> contactInfo = userService.getUsersContactInfo();
        return ResponseEntity.ok(contactInfo);
    }

}

