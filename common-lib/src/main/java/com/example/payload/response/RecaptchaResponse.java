package com.example.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class RecaptchaResponse {

    private boolean success;

    private Double score;

    private String action;

    @JsonProperty("error-codes")
    private List<String> errorCodes;
}
