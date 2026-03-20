package com.gigshield.dto.response;

import com.gigshield.model.enums.CoverageTier;
import com.gigshield.model.enums.PolicyStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PolicyResponse {
    private Long id;
    private String policyNumber;
    private CoverageTier coverageTier;
    private BigDecimal weeklyPremium;
    private BigDecimal maxWeeklyPayout;
    private LocalDate startDate;
    private LocalDate endDate;
    private PolicyStatus status;
    private Boolean autoRenew;
    private String triggersCovered;
    private LocalDateTime createdAt;
    private Long workerId;
    private String workerName;
    private String workerCity;
}
