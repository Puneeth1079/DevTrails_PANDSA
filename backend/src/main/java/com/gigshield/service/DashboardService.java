package com.gigshield.service;

import com.gigshield.dto.response.DashboardAdminResponse;
import com.gigshield.dto.response.DashboardWorkerResponse;
import com.gigshield.model.Policy;
import com.gigshield.model.WorkerProfile;
import com.gigshield.model.enums.ClaimStatus;
import com.gigshield.repository.ClaimRepository;
import com.gigshield.repository.DisruptionEventRepository;
import com.gigshield.repository.PolicyRepository;
import com.gigshield.repository.WorkerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PolicyService policyService;
    private final ClaimService claimService;
    private final PayoutService payoutService;
    private final WorkerService workerService;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final DisruptionEventRepository disruptionEventRepository;
    private final WorkerProfileRepository workerProfileRepository;

    public DashboardWorkerResponse getWorkerDashboard(Long workerId) {
        WorkerProfile worker = workerService.getWorkerById(workerId);
        Policy activePolicy = policyService.getActivePolicy(workerId);

        // Active disruption alerts for worker's city
        List<String> alerts = disruptionEventRepository
                .findByCityAndIsActiveTrue(worker.getCity())
                .stream()
                .map(e -> "⚠️ " + e.getTriggerType().name().replace("_", " ") + " alert in " + e.getCity())
                .collect(Collectors.toList());

        // Recent claims (last 5)
        var recentClaims = claimRepository.findByWorkerId(workerId,
                org.springframework.data.domain.PageRequest.of(0, 5))
                .stream()
                .map(claimService::toClaimResponse)
                .collect(Collectors.toList());

        BigDecimal totalPayout = payoutService.getTotalPayoutForWorker(workerId);
        Long claimsThisMonth = claimService.countClaimsThisMonth(workerId);

        // Weekly chart (mock last 8 weeks)
        List<Map<String, Object>> weeklyHistory = generateWeeklyHistory(activePolicy);

        return DashboardWorkerResponse.builder()
                .activePolicy(activePolicy != null ? policyService.toPolicyResponse(activePolicy) : null)
                .earningsProtected(activePolicy != null ? activePolicy.getMaxWeeklyPayout() : BigDecimal.ZERO)
                .totalClaimsThisMonth(claimsThisMonth)
                .totalPayoutReceived(totalPayout)
                .currentZoneAlerts(alerts)
                .recentClaims(recentClaims)
                .weeklyProtectionHistory(weeklyHistory)
                .riskLevel(getRiskLevel(worker.getRiskScore()))
                .build();
    }

    public DashboardAdminResponse getAdminDashboard() {
        long totalWorkers = workerService.getTotalWorkerCount();
        long activePolicies = policyService.countActivePolicies();
        long pendingClaims = claimService.countPendingClaims();
        BigDecimal totalPayouts = payoutService.getTotalPayoutsThisMonth();
        long fraudAlerts = fraudAlertCount();

        // Claims by trigger type
        Map<String, Long> claimsByType = claimRepository.countByTriggerType()
                .stream().collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> (Long) row[1]
                ));

        // City-wise risk
        List<Map<String, Object>> cityRisk = generateCityRiskTable();

        // Revenue vs payouts (last 8 weeks mock)
        List<Map<String, Object>> revenuePayout = generateRevenueVsPayouts();

        // Loss ratio
        double lossRatio = totalPayouts.compareTo(BigDecimal.ZERO) > 0
                ? totalPayouts.divide(BigDecimal.valueOf(activePolicies * 65L + 1), 4, RoundingMode.HALF_UP).doubleValue()
                : 0.62;

        // Predictive alerts
        List<Map<String, Object>> predictive = List.of(
                Map.of("city", "Mumbai", "risk", "HIGH", "nextWeekForecast", "Heavy monsoon expected"),
                Map.of("city", "Delhi", "risk", "MEDIUM", "nextWeekForecast", "AQI likely to rise"),
                Map.of("city", "Chennai", "risk", "LOW", "nextWeekForecast", "Clear weather expected")
        );

        return DashboardAdminResponse.builder()
                .totalWorkers(totalWorkers)
                .activePolicies(activePolicies)
                .pendingClaims(pendingClaims)
                .totalPayoutsThisMonth(totalPayouts)
                .lossRatio(Math.min(1.0, lossRatio))
                .fraudAlertsActive(fraudAlerts)
                .claimsByTriggerType(claimsByType)
                .cityWiseRisk(cityRisk)
                .revenueVsPayouts(revenuePayout)
                .predictiveAlerts(predictive)
                .build();
    }

    private long fraudAlertCount() {
        return claimRepository.countByStatus(ClaimStatus.PENDING_REVIEW);
    }

    private List<Map<String, Object>> generateWeeklyHistory(Policy activePolicy) {
        List<Map<String, Object>> history = new ArrayList<>();
        for (int i = 7; i >= 0; i--) {
            Map<String, Object> week = new HashMap<>();
            week.put("week", "Week " + (8 - i));
            week.put("premium", activePolicy != null ? activePolicy.getWeeklyPremium() : 65);
            week.put("payout", i % 3 == 0 ? 450 : 0);
            history.add(week);
        }
        return history;
    }

    private List<Map<String, Object>> generateCityRiskTable() {
        String[] cities = {"Mumbai", "Delhi", "Bengaluru", "Chennai", "Hyderabad"};
        String[] risks = {"HIGH", "HIGH", "MEDIUM", "MEDIUM", "LOW"};
        List<Map<String, Object>> table = new ArrayList<>();
        for (int i = 0; i < cities.length; i++) {
            long activeCount = policyRepository.findActivePoliciesByCity(cities[i]).size();
            Map<String, Object> row = new HashMap<>();
            row.put("city", cities[i]);
            row.put("activePolicies", activeCount);
            row.put("claimsThisWeek", (int)(Math.random() * 20));
            row.put("avgRiskScore", 40 + (i * 10));
            row.put("riskStatus", risks[i]);
            table.add(row);
        }
        return table;
    }

    private List<Map<String, Object>> generateRevenueVsPayouts() {
        List<Map<String, Object>> data = new ArrayList<>();
        String[] weeks = {"W1", "W2", "W3", "W4", "W5", "W6", "W7", "W8"};
        int[] premiums = {45000, 52000, 48000, 61000, 59000, 67000, 72000, 78000};
        int[] payouts = {18000, 24000, 35000, 22000, 41000, 38000, 29000, 45000};
        for (int i = 0; i < weeks.length; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("week", weeks[i]);
            row.put("premiums", premiums[i]);
            row.put("payouts", payouts[i]);
            data.add(row);
        }
        return data;
    }

    private String getRiskLevel(BigDecimal riskScore) {
        if (riskScore == null) return "MEDIUM";
        int score = riskScore.intValue();
        if (score < 35) return "LOW";
        if (score < 65) return "MEDIUM";
        return "HIGH";
    }
}
