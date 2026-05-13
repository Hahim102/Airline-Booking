package com.example.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailEvent {

    private String to;

    private String subject;

    private String template;

    private Map<String, Object> data;
}
