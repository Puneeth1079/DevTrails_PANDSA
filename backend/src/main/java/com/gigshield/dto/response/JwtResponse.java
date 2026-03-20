package com.gigshield.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtResponse {
    private String token;
    private String role;
    private String name;
    private String mobile;
    private Long userId;
    private Long workerId;  // null for admins
}
