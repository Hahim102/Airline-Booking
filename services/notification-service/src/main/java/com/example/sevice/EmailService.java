package com.example.sevice;

import com.example.payload.dto.MessageDTO;

public interface EmailService {

    void sendEmail(MessageDTO messageDTO);

}
