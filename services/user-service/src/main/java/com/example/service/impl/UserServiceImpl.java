package com.example.service.impl;

import com.example.model.Users;
import com.example.payload.response.UserResponse;
import com.example.repository.UserRepository;
import com.example.service.UserService;
import com.example.util.ModelMapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;

    @Override
    public UserResponse getUserByEmail(String email) throws Exception {
        Users users = userRepository.findByEmail(email);

        if(users == null) {
            throw new Exception("User not found with email: " + email);
        }
        return ModelMapperUtil.mapper(users, UserResponse.class);
    }

    @Override
    public UserResponse getUserById(Long id) throws Exception {
            Users users = userRepository.findById(id).orElse(null);
            if(users == null) {
                throw new Exception("User not found with id: " + id);
            }
            return ModelMapperUtil.mapper(users, UserResponse.class);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<Users> users = userRepository.findAll();
        return ModelMapperUtil.mapList(users, UserResponse.class);
    }
}
