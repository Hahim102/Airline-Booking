package com.example.controller;


import com.example.payload.dto.UserDTO;
import com.example.payload.response.UserResponse;
import com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}