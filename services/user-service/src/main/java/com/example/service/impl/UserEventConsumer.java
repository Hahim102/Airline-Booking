package com.example.service.impl;

import com.example.event.UserRegisteredEvent;
import com.example.model.Users;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventConsumer {
    private final UserRepository userRepository;

    @KafkaListener(
            topics = "user-registered-events",
            groupId = "user-service-group"
    )
    public void handleUserRegistered(UserRegisteredEvent event) {

        if (userRepository.existsById(event.getUserId())) {
            return;
        }

        Users user = Users.builder()
                .id(event.getUserId())
                .email(event.getEmail())
                .fullName(event.getFullName())
                .phone(event.getPhone())
                .role(event.getRole())
                .active(event.isActive())
                .deleted(false)
                .createdAt(event.getCreatedAt())
                .build();

        userRepository.save(user);
    }
}
