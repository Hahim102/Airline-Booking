package com.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedToken {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "blacklist_seq")
    @SequenceGenerator(
            name = "blacklist_seq",
            sequenceName = "BLACKLISTED_TOKEN_SEQ",
            allocationSize = 1
    )
    private Long id;

    private String token;
    private Date expiredAt;
}
