package com.example.service.Impl;


import com.example.payload.response.RecaptchaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class RecaptchaService {


    @Value("${recaptcha.secret}")
    private String secret;

    @Value("${recaptcha.verify-url}")
    private String verifyUrl;

    private final RestClient restClient;

    public RecaptchaResponse verify(String token) {


        if (token == null || token.isBlank()) {
            RecaptchaResponse response = new RecaptchaResponse();
            response.setSuccess(false);

            return response;
        }

        return restClient.post()
                .uri(
                        verifyUrl
                                + "?secret=" + secret
                                + "&response=" + token
                )
                .retrieve()
                .body(RecaptchaResponse.class);

    }
}
