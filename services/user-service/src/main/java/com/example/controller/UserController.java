package com.example.controller;


import com.example.payload.dto.UserDTO;
import com.example.payload.request.CreateUserRequest;
import com.example.payload.response.CreateUserResponse;
import com.example.payload.response.UserResponse;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.CacheRequest;
import java.util.List;

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
    public ResponseEntity<UserResponse> updateIsActive(
            @PathVariable Long userId,
            @RequestParam boolean isActive) throws Exception {
        UserResponse user = userService.updateIsActiveStatus(userId, isActive);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUserById(@PathVariable Long userId) throws Exception {
        userService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PostMapping("/create-user")
    public ResponseEntity<CreateUserResponse> createUser(@RequestBody CreateUserRequest request) throws Exception {
        CreateUserResponse createdUser = userService.createUser(request);
        return ResponseEntity.ok(createdUser);
    }

}