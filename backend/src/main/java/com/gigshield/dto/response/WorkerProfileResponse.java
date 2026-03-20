package com.gigshield.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WorkerProfileResponse {
    private Long id;
    private Long userId;
    private String name;
    private String mobile;
    private String email;
    private String platform;
    private String platformPartnerId;
    private String city;
    private String zone;
    private String pincode;
    private BigDecimal avgDailyEarnings;
    private BigDecimal avgDailyHours;
    private String upiId;
    private String bankAccount;
    private String ifsc;
    private BigDecimal riskScore;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
