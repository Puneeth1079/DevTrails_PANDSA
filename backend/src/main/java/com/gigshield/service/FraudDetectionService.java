package com.gigshield.service;

import com.gigshield.model.Claim;
import com.gigshield.model.WorkerProfile;
import com.gigshield.model.enums.ClaimStatus;
import com.gigshield.repository.ClaimRepository;
import com.gigshield.repository.PayoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {

    private final ClaimRepository claimRepository;
    private final PayoutRepository payoutRepository;

    public BigDecimal scoreClaim(Claim claim, List<String> fraudFlags) {
        double score = 0.0;
        WorkerProfile worker = claim.getWorker();

        // Check 1: Location Validation (0–30 points)
        if (claim.getWorkerLocationLat() != null && claim.getDisruptionEvent() != null) {
            // Simple heuristic: if lat/lng is far outside expected city bounds → suspicious
            // In production, use geodesic distance to disruption event center
        }

        // Check 2: Duplicate Claim Prevention (0–30 points)
        LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
        List<Claim> recentSameTrigger = claimRepository.findRecentClaimsByWorkerAndType(
                worker.getId(), claim.getTriggerType(), yesterday);
        if (recentSameTrigger.size() > 1) {
            score += 30;
            fraudFlags.add("DUPLICATE_CLAIM_WITHIN_24H");
            log.warn("Fraud flag: Duplicate claim for worker {} trigger {}", worker.getId(), claim.getTriggerType());
        }

        // Check 3: Activity Pattern Anomaly (0–20 points)
        List<Claim> last7Days = claimRepository.findRecentClaimsByWorker(
                worker.getId(), LocalDateTime.now().minusDays(7));
        if (last7Days.isEmpty()) {
            score += 15;
            fraudFlags.add("INACTIVE_WORKER");
        }

        // Sudden policy subscription (policy age < 1 day)
        if (claim.getPolicy() != null && claim.getPolicy().getCreatedAt() != null) {
            long policyAgeHours = java.time.Duration.between(
                    claim.getPolicy().getCreatedAt(), LocalDateTime.now()).toHours();
            if (policyAgeHours < 24) {
                score += 10;
                fraudFlags.add("NEW_POLICY_CLAIM");
            }
        }

        // Check 4: Payout Frequency (0–20 points)
        List<Claim> last30Days = claimRepository.findRecentClaimsByWorker(
                worker.getId(), LocalDateTime.now().minusDays(30));
        if (last30Days.size() > 3) {
            score += 10;
            fraudFlags.add("HIGH_CLAIM_FREQUENCY");
        }

        // Payout amount vs daily earnings
        if (worker.getAvgDailyEarnings() != null && claim.getPayoutAmount() != null) {
            BigDecimal limit = worker.getAvgDailyEarnings().multiply(BigDecimal.valueOf(1.5));
            if (claim.getPayoutAmount().compareTo(limit) > 0) {
                score += 15;
                fraudFlags.add("PAYOUT_EXCEEDS_EARNINGS");
            }
        }

        return BigDecimal.valueOf(Math.min(100, score));
    }

    public ClaimStatus determineClaimStatus(BigDecimal fraudScore) {
        if (fraudScore.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return ClaimStatus.PENDING_REVIEW;
        } else if (fraudScore.compareTo(BigDecimal.valueOf(30)) >= 0) {
            return ClaimStatus.PENDING_REVIEW;
        }
        return ClaimStatus.AUTO_APPROVED;
    }

    public long countFraudAlerts() {
        return claimRepository.countByStatus(ClaimStatus.PENDING_REVIEW);
    }
}
