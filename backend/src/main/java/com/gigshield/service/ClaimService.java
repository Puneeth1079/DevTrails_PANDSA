package com.gigshield.service;

import com.gigshield.dto.response.ClaimResponse;
import com.gigshield.model.Claim;
import com.gigshield.model.DisruptionEvent;
import com.gigshield.model.Policy;
import com.gigshield.model.WorkerProfile;
import com.gigshield.model.enums.ClaimStatus;
import com.gigshield.model.enums.TriggerType;
import com.gigshield.repository.ClaimRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final FraudDetectionService fraudDetectionService;
    private final PayoutService payoutService;
    private final ObjectMapper objectMapper;

    @Transactional
    public Claim autoTriggerClaim(Policy policy, DisruptionEvent event, BigDecimal hoursLost) {
        WorkerProfile worker = policy.getWorker();

        // Calculate payout
        BigDecimal payoutAmount = calculateAutoPayout(policy, hoursLost);

        List<String> fraudFlags = new ArrayList<>();
        Claim claim = Claim.builder()
                .claimNumber(generateClaimNumber())
                .policy(policy)
                .worker(worker)
                .disruptionEvent(event)
                .triggerType(event.getTriggerType())
                .hoursLost(hoursLost)
                .payoutAmount(payoutAmount)
                .autoTriggered(true)
                .status(ClaimStatus.AUTO_APPROVED)
                .fraudScore(BigDecimal.ZERO)
                .build();

        claim = claimRepository.save(claim);

        // Score fraud
        BigDecimal fraudScore = fraudDetectionService.scoreClaim(claim, fraudFlags);
        claim.setFraudScore(fraudScore);
        try {
            claim.setFraudFlags(objectMapper.writeValueAsString(fraudFlags));
        } catch (Exception e) {
            claim.setFraudFlags("[]");
        }
        claim.setStatus(fraudDetectionService.determineClaimStatus(fraudScore));
        claim.setProcessedAt(LocalDateTime.now());
        claim = claimRepository.save(claim);

        log.info("Auto-triggered claim {} for worker {} | trigger={} | payout=₹{} | fraud={}",
                claim.getClaimNumber(), worker.getId(), event.getTriggerType(), payoutAmount, fraudScore);

        // Initiate payout if auto-approved
        if (claim.getStatus() == ClaimStatus.AUTO_APPROVED) {
            payoutService.initiateAutoPayout(claim);
        }

        return claim;
    }

    @Transactional
    public Claim updateClaimStatus(Long claimId, ClaimStatus newStatus, String notes) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found: " + claimId));
        claim.setStatus(newStatus);
        if (notes != null) claim.setNotes(notes);
        claim.setProcessedAt(LocalDateTime.now());
        claim = claimRepository.save(claim);

        if (newStatus == ClaimStatus.APPROVED) {
            payoutService.initiateAutoPayout(claim);
        }
        return claim;
    }

    public Page<Claim> getWorkerClaims(Long workerId, Pageable pageable) {
        return claimRepository.findByWorkerId(workerId, pageable);
    }

    public Page<Claim> getAllClaims(ClaimStatus status, Pageable pageable) {
        if (status != null) {
            return claimRepository.findByStatus(status, pageable);
        }
        return claimRepository.findAll(pageable);
    }

    public long countPendingClaims() {
        return claimRepository.countByStatus(ClaimStatus.PENDING_REVIEW);
    }

    public Long countClaimsThisMonth(Long workerId) {
        return claimRepository.countClaimsThisMonth(workerId);
    }

    private BigDecimal calculateAutoPayout(Policy policy, BigDecimal hoursLost) {
        if (hoursLost == null) hoursLost = BigDecimal.valueOf(4);
        BigDecimal maxPayout = policy.getMaxWeeklyPayout();
        BigDecimal dailyHours = policy.getWorker().getAvgDailyHours() != null
                ? policy.getWorker().getAvgDailyHours() : BigDecimal.valueOf(8);
        BigDecimal ratio = hoursLost.divide(dailyHours, 4, java.math.RoundingMode.HALF_UP);
        return ratio.multiply(maxPayout).min(maxPayout)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String generateClaimNumber() {
        return "CLM-" + System.currentTimeMillis() + "-" + (new Random().nextInt(900) + 100);
    }

    public ClaimResponse toClaimResponse(Claim claim) {
        return ClaimResponse.builder()
                .id(claim.getId())
                .claimNumber(claim.getClaimNumber())
                .policyNumber(claim.getPolicy() != null ? claim.getPolicy().getPolicyNumber() : null)
                .triggerType(claim.getTriggerType())
                .hoursLost(claim.getHoursLost())
                .payoutAmount(claim.getPayoutAmount())
                .status(claim.getStatus())
                .fraudScore(claim.getFraudScore())
                .fraudFlags(claim.getFraudFlags())
                .autoTriggered(claim.getAutoTriggered())
                .notes(claim.getNotes())
                .claimedAt(claim.getClaimedAt())
                .processedAt(claim.getProcessedAt())
                .workerId(claim.getWorker() != null ? claim.getWorker().getId() : null)
                .workerName(claim.getWorker() != null && claim.getWorker().getUser() != null
                        ? claim.getWorker().getUser().getName() : null)
                .city(claim.getWorker() != null ? claim.getWorker().getCity() : null)
                .severityValue(claim.getDisruptionEvent() != null ? claim.getDisruptionEvent().getSeverityValue() : null)
                .severityUnit(claim.getDisruptionEvent() != null ? claim.getDisruptionEvent().getSeverityUnit() : null)
                .build();
    }
}
