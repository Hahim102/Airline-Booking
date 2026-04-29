package com.example.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "refresh_seq")
    @SequenceGenerator(
            name = "refresh_seq",
            sequenceName = "REFRESH_TOKEN_SEQ",
            allocationSize = 1
    )
    private Long id;

    private String token;

    private Long userId;

    private Date expiryDate;
}
