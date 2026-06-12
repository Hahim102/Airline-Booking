package com.example.service.Impl;

import com.example.event.UserUpdatedEvent;
import com.example.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserUpdatedConsumer {

    private final AuthUserRepository authUserRepository;

    @KafkaListener(
            topics = "user-updated-events",
            groupId = "auth-service-group"
    )
    public void consume(UserUpdatedEvent event) {

        authUserRepository.findById(event.getUserId())
                .ifPresent(user -> {

                    user.setEmail(event.getEmail());
                    user.setFullName(event.getFullName());
                    user.setPhone(event.getPhone());
                    user.setUpdatedAt(event.getUpdatedAt());

                    authUserRepository.save(user);
                });
    }
}
