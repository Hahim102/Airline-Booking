package com.example.sevice.impl;

import com.example.event.EmailEvent;
import com.example.sevice.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailConsumer {

    private final EmailService emailService;

    @KafkaListener(
            topics = "email-topic",
            groupId = "notification-group"
    )
    public void consume(EmailEvent event) {

        emailService.sendMail(event);
    }
}