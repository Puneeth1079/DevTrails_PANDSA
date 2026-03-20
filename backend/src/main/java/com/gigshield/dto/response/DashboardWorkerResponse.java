package com.gigshield.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardWorkerResponse {
    private PolicyResponse activePolicy;
    private BigDecimal earningsProtected;
    private Long totalClaimsThisMonth;
    private BigDecimal totalPayoutReceived;
    private List<String> currentZoneAlerts;
    private List<ClaimResponse> recentClaims;
    private List<Map<String, Object>> weeklyProtectionHistory;
    private String currentWeatherCondition;
    private Double currentTemp;
    private Integer currentAqi;
    private String riskLevel;
}
