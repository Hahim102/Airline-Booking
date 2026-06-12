package com.example.service.Impl;

import com.example.event.EmailEvent;
import com.example.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEmailEvent(EmailEvent event) {

        kafkaTemplate.send("email-topic", event);
    }

    public void sendUserRegisteredEvent(UserRegisteredEvent event) {
        kafkaTemplate.send("user-registered-events", event);
    }
}
