package com.example.sevice.impl;


import java.nio.charset.StandardCharsets;

import com.example.enums.ErrorCode;
import com.example.event.EmailEvent;
import com.example.exception.AppException;
import com.example.payload.dto.MessageDTO;
import com.example.sevice.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;


@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;


    private final JavaMailSender mailSender;

    public void sendMail(EmailEvent event) {

        try {
            Context context = new Context();

            event.getData().forEach(context::setVariable);

            String html = templateEngine.process(
                    getTemplateFile(event.getTemplate()),
                    context
            );

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            helper.setTo(event.getTo());
            helper.setSubject(event.getSubject());
            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new AppException(ErrorCode.MAIL_SERVER_ERROR);
        }
    }
    private String getTemplateFile(String template) {

        return switch (template) {
            case "WELCOME" -> "welcome-email";
            case "VERIFY_OTP" -> "verify-otp";
            case "LOGIN_SUCCESS" -> "login-success";
            case "RESET_PASSWORD_OTP" -> "reset-password-otp";
            default -> throw new RuntimeException(
                    "Template not found: " + template
            );
        };
    }


    @Override
    public void sendEmail(MessageDTO messageDTO) {
        try {
            logger.info("START... Sending email");

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());

            Context context = new Context();
            context.setVariable("name", messageDTO.getToName());
            context.setVariable("content", messageDTO.getContent());
            String html = templateEngine.process("welcome-email", context);

            helper.setTo(messageDTO.getTo());
            helper.setText(html, true);
            helper.setSubject(messageDTO.getSubject());
            helper.setFrom(messageDTO.getFrom());
            javaMailSender.send(message);

            logger.info("END... Email sent success");
        } catch (MessagingException e) {
            logger.error("Email sent with error: " + e.getMessage());
        }
    }
}
