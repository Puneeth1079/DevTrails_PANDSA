package com.gigshield.service;

import com.gigshield.dto.request.PolicySubscribeRequest;
import com.gigshield.dto.response.PolicyResponse;
import com.gigshield.model.Policy;
import com.gigshield.model.WorkerProfile;
import com.gigshield.model.enums.CoverageTier;
import com.gigshield.model.enums.PolicyStatus;
import com.gigshield.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final PremiumCalculationService premiumCalculationService;

    @Transactional
    public Policy subscribePolicy(WorkerProfile worker, PolicySubscribeRequest req) {
        // Check existing active policy
        List<Policy> activePolicies = policyRepository.findByWorkerIdAndStatus(worker.getId(), PolicyStatus.ACTIVE);
        if (!activePolicies.isEmpty()) {
            throw new RuntimeException("Worker already has an active policy. Please cancel it first.");
        }

        // Count renewals for loyalty discount
        Page<Policy> allPolicies = policyRepository.findByWorkerId(worker.getId(), Pageable.unpaged());
        int renewalCount = (int) allPolicies.stream()
                .filter(p -> p.getStatus() == PolicyStatus.EXPIRED || p.getStatus() == PolicyStatus.ACTIVE)
                .count();

        BigDecimal premium = premiumCalculationService.calculateWeeklyPremium(
                req.getCoverageTier(),
                worker.getCity(), worker.getZone(),
                worker.getAvgDailyEarnings(), worker.getPlatform(),
                renewalCount, req.isAutoRenew()
        );

        BigDecimal maxPayout = premiumCalculationService.getMaxWeeklyPayout(req.getCoverageTier());

        String triggersCovered = getTriggersCoveredJson(req.getCoverageTier());

        Policy policy = Policy.builder()
                .worker(worker)
                .policyNumber(generatePolicyNumber())
                .coverageTier(req.getCoverageTier())
                .weeklyPremium(premium)
                .maxWeeklyPayout(maxPayout)
                .startDate(req.getStartDate())
                .endDate(req.getStartDate().plusDays(7))
                .status(PolicyStatus.ACTIVE)
                .autoRenew(req.isAutoRenew())
                .triggersCovered(triggersCovered)
                .build();

        policy = policyRepository.save(policy);
        log.info("Policy {} created for worker {} in {}", policy.getPolicyNumber(), worker.getId(), worker.getCity());
        return policy;
    }

    public Page<Policy> getWorkerPolicies(Long workerId, Pageable pageable) {
        return policyRepository.findByWorkerId(workerId, pageable);
    }

    public Policy getActivePolicy(Long workerId) {
        return policyRepository.findByWorkerIdAndStatus(workerId, PolicyStatus.ACTIVE)
                .stream().findFirst().orElse(null);
    }

    @Transactional
    public Policy cancelPolicy(Long policyId, Long workerId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));
        if (!policy.getWorker().getId().equals(workerId)) {
            throw new RuntimeException("Unauthorized to cancel this policy");
        }
        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new RuntimeException("Only active policies can be cancelled");
        }
        policy.setStatus(PolicyStatus.CANCELLED);
        return policyRepository.save(policy);
    }

    public long countActivePolicies() {
        return policyRepository.countByStatus(PolicyStatus.ACTIVE);
    }

    public List<Policy> findActivePoliciesByCity(String city) {
        return policyRepository.findActivePoliciesByCity(city);
    }

    public List<Policy> findExpiringSoon() {
        LocalDate today = LocalDate.now();
        return policyRepository.findExpiringSoon(today, today.plusDays(2));
    }

    private String generatePolicyNumber() {
        int year = Year.now().getValue();
        long random = (long) (Math.random() * 99999999L) + 10000000L;
        return "GS-" + year + "-" + random;
    }

    private String getTriggersCoveredJson(CoverageTier tier) {
        return switch (tier) {
            case BASIC -> "[\"HEAVY_RAIN\",\"EXTREME_HEAT\"]";
            case STANDARD -> "[\"HEAVY_RAIN\",\"EXTREME_HEAT\",\"SEVERE_POLLUTION\",\"CURFEW\",\"FLOOD\"]";
            case PREMIUM -> "[\"HEAVY_RAIN\",\"EXTREME_HEAT\",\"SEVERE_POLLUTION\",\"CURFEW\",\"FLOOD\"]";
        };
    }

    public PolicyResponse toPolicyResponse(Policy policy) {
        return PolicyResponse.builder()
                .id(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .coverageTier(policy.getCoverageTier())
                .weeklyPremium(policy.getWeeklyPremium())
                .maxWeeklyPayout(policy.getMaxWeeklyPayout())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .status(policy.getStatus())
                .autoRenew(policy.getAutoRenew())
                .triggersCovered(policy.getTriggersCovered())
                .createdAt(policy.getCreatedAt())
                .workerId(policy.getWorker().getId())
                .workerName(policy.getWorker().getUser().getName())
                .workerCity(policy.getWorker().getCity())
                .build();
    }
}
