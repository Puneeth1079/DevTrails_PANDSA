package com.gigshield.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardAdminResponse {
    private Long totalWorkers;
    private Long activePolicies;
    private Long pendingClaims;
    private BigDecimal totalPayoutsThisMonth;
    private Double lossRatio;
    private Long fraudAlertsActive;
    private Map<String, Long> claimsByTriggerType;
    private List<Map<String, Object>> cityWiseRisk;
    private List<Map<String, Object>> revenueVsPayouts;
    private List<Map<String, Object>> predictiveAlerts;
    private BigDecimal totalPremiumsCollected;
    private Long totalWorkersActive;
}
