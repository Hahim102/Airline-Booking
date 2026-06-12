package com.example.service.impl;

import com.example.event.UserUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendUserUpdatedEvent(UserUpdatedEvent event) {
        kafkaTemplate.send("user-updated-events", event);
    }
}
