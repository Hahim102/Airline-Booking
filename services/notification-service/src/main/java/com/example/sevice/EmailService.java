package com.example.sevice;

import com.example.event.EmailEvent;
import com.example.payload.dto.MessageDTO;

public interface EmailService {

    public void sendMail(EmailEvent event);
    void sendEmail(MessageDTO messageDTO);

}
