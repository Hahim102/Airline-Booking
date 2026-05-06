package com.example.service.impl;

import com.example.model.Users;
import com.example.payload.dto.UserDTO;
import com.example.payload.response.UserResponse;
import com.example.repository.UserRepository;
import com.example.service.UserService;
import com.example.util.ModelMapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;

    @Override
    public UserResponse getUserByEmail(String email) throws Exception {
        Users users = userRepository.findByEmailAndDeletedIsFalse(email);

        if(users == null) {
            throw new Exception("User not found with email: " + email);
        }
        return ModelMapperUtil.mapper(users, UserResponse.class);
    }

    @Override
    public UserResponse getUserById(Long id) throws Exception {
        Users users = userRepository.findByIdAndDeletedIsFalse(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ModelMapperUtil.mapper(users, UserResponse.class);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<Users> users = userRepository.findAllByDeletedIsFalse();
        return ModelMapperUtil.mapList(users, UserResponse.class);
    }

    @Override
    public void updateIsActiveStatus(Long userId, boolean isActive) throws Exception {
        Users user = userRepository.findByIdAndDeletedIsFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(isActive);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) throws Exception {
        Users user = userRepository.findByIdAndDeletedIsFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setDeleted(true);
        user.setActive(false);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }


}
