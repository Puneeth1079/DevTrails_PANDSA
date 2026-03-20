package com.gigshield.controller;

import com.gigshield.dto.request.PolicySubscribeRequest;
import com.gigshield.dto.response.ApiResponse;
import com.gigshield.dto.response.PolicyResponse;
import com.gigshield.model.Policy;
import com.gigshield.model.WorkerProfile;
import com.gigshield.model.enums.CoverageTier;
import com.gigshield.security.JwtTokenProvider;
import com.gigshield.service.PolicyService;
import com.gigshield.service.PremiumCalculationService;
import com.gigshield.service.WorkerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Tag(name = "Policies", description = "Insurance policy management")
public class PolicyController {

    private final PolicyService policyService;
    private final PremiumCalculationService premiumService;
    private final WorkerService workerService;
    private final JwtTokenProvider tokenProvider;

    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPlans() {
        List<Map<String, Object>> plans = List.of(
                Map.of("tier", "BASIC", "basePremium", 35, "minPremium", 29, "maxPremium", 45,
                        "maxWeeklyPayout", 500, "triggers", List.of("HEAVY_RAIN", "EXTREME_HEAT"),
                        "features", List.of("2 trigger types", "₹500 max payout/week", "Auto-claim")),
                Map.of("tier", "STANDARD", "basePremium", 65, "minPremium", 59, "maxPremium", 79,
                        "maxWeeklyPayout", 900, "triggers", List.of("HEAVY_RAIN", "EXTREME_HEAT", "SEVERE_POLLUTION", "CURFEW", "FLOOD"),
                        "features", List.of("5 trigger types", "₹900 max payout/week", "Auto-claim", "Priority support")),
                Map.of("tier", "PREMIUM", "basePremium", 109, "minPremium", 99, "maxPremium", 129,
                        "maxWeeklyPayout", 1500, "triggers", List.of("HEAVY_RAIN", "EXTREME_HEAT", "SEVERE_POLLUTION", "CURFEW", "FLOOD"),
                        "features", List.of("5 trigger types", "₹1500 max payout/week", "Enhanced fraud protection", "Instant payout", "24/7 support"))
        );
        return ResponseEntity.ok(ApiResponse.success(plans));
    }

    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<ApiResponse<PolicyResponse>> subscribe(
            @Valid @RequestBody PolicySubscribeRequest req, HttpServletRequest request) {
        Long userId = getUserId(request);
        WorkerProfile worker = workerService.getWorkerByUserId(userId);
        Policy policy = policyService.subscribePolicy(worker, req);
        return ResponseEntity.ok(ApiResponse.success(policyService.toPolicyResponse(policy), "Policy subscribed successfully"));
    }

    @GetMapping("/my-policies")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<ApiResponse<Page<PolicyResponse>>> getMyPolicies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        WorkerProfile worker = workerService.getWorkerByUserId(userId);
        Page<PolicyResponse> policies = policyService.getWorkerPolicies(worker.getId(), PageRequest.of(page, size))
                .map(policyService::toPolicyResponse);
        return ResponseEntity.ok(ApiResponse.success(policies));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<ApiResponse<PolicyResponse>> cancelPolicy(
            @PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        WorkerProfile worker = workerService.getWorkerByUserId(userId);
        Policy policy = policyService.cancelPolicy(id, worker.getId());
        return ResponseEntity.ok(ApiResponse.success(policyService.toPolicyResponse(policy), "Policy cancelled"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PolicyResponse>>> getAllPolicies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<PolicyResponse> policies = policyService.getWorkerPolicies(null, PageRequest.of(page, size))
                .map(policyService::toPolicyResponse);
        return ResponseEntity.ok(ApiResponse.success(policies));
    }

    @GetMapping("/premium-estimate")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> premiumEstimate(
            @RequestParam CoverageTier tier, HttpServletRequest request) {
        Long userId = getUserId(request);
        WorkerProfile worker = workerService.getWorkerByUserId(userId);
        BigDecimal premium = premiumService.calculateWeeklyPremium(
                tier, worker.getCity(), worker.getZone(),
                worker.getAvgDailyEarnings(), worker.getPlatform(), 0, false);
        return ResponseEntity.ok(ApiResponse.success(Map.of("weeklyPremium", premium)));
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return tokenProvider.getUserIdFromToken(token);
    }
}
