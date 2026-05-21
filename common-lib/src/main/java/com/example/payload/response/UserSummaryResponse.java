package com.example.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {
    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long deletedUsers;
}
