package com.gigshield.service;

import com.gigshield.model.Policy;
import com.gigshield.model.Worker;
import com.gigshield.model.Zone;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;

/**
 * AI-Powered Premium Calculation Engine
 *
 * Calculates dynamic weekly premium based on:
 *  - Zone risk level (flood-prone, heat-prone)
 *  - Season / weather forecast factor
 *  - Worker's historical claim record
 *  - Coverage tier chosen
 */
@Service
public class PremiumCalculationService {

    // Base weekly premiums per tier (INR)
    private static final BigDecimal BASE_BASIC    = new BigDecimal("29");
    private static final BigDecimal BASE_STANDARD = new BigDecimal("49");
    private static final BigDecimal BASE_PRO      = new BigDecimal("79");

    // Coverage amounts per tier (INR)
    private static final BigDecimal COVERAGE_BASIC    = new BigDecimal("500");
    private static final BigDecimal COVERAGE_STANDARD = new BigDecimal("1000");
    private static final BigDecimal COVERAGE_PRO      = new BigDecimal("1800");

    /**
     * Main method: Calculate dynamic weekly premium for a worker
     *
     * Formula:
     * Final Premium = Base Rate x Zone Multiplier x Season Multiplier x History Multiplier
     */
    public PremiumResult calculateWeeklyPremium(Worker worker, Policy.PlanType planType) {
        BigDecimal baseRate = getBaseRate(planType);
        BigDecimal coverage = getCoverageAmount(planType);

        // Step 1: Zone Risk Multiplier
        BigDecimal zoneMultiplier = calculateZoneMultiplier(worker.getZone());

        // Step 2: Season / Weather Multiplier
        BigDecimal seasonMultiplier = calculateSeasonMultiplier();

        // Step 3: Worker History Multiplier
        BigDecimal historyMultiplier = calculateHistoryMultiplier(worker.getRiskScore());

        // Final calculation
        BigDecimal finalPremium = baseRate
                .multiply(zoneMultiplier)
                .multiply(seasonMultiplier)
                .multiply(historyMultiplier)
                .setScale(2, RoundingMode.HALF_UP);

        return PremiumResult.builder()
                .basePremium(baseRate)
                .finalPremium(finalPremium)
                .coverageAmount(coverage)
                .zoneMultiplier(zoneMultiplier)
                .seasonMultiplier(seasonMultiplier)
                .historyMultiplier(historyMultiplier)
                .planType(planType)
                .build();
    }

    /**
     * Zone multiplier based on flood/heat risk of delivery zone
     * Flood-prone zone = 1.3x | High risk = 1.2x | Low risk = 0.9x
     */
    private BigDecimal calculateZoneMultiplier(Zone zone) {
        if (zone == null) return BigDecimal.ONE;
        switch (zone.getRiskLevel()) {
            case VERY_HIGH: return new BigDecimal("1.40");
            case HIGH:      return new BigDecimal("1.25");
            case MEDIUM:    return new BigDecimal("1.00");
            case LOW:       return new BigDecimal("0.90");
            default:        return BigDecimal.ONE;
        }
    }

    /**
     * Season multiplier based on current month
     * Monsoon (June-Sept) = 1.2x | Summer (Mar-May) = 1.1x | Winter = 1.0x
     */
    private BigDecimal calculateSeasonMultiplier() {
        Month currentMonth = LocalDate.now().getMonth();
        switch (currentMonth) {
            case JUNE: case JULY: case AUGUST: case SEPTEMBER:
                return new BigDecimal("1.20"); // Monsoon
            case MARCH: case APRIL: case MAY:
                return new BigDecimal("1.10"); // Summer / Heat season
            case OCTOBER: case NOVEMBER:
                return new BigDecimal("1.05"); // Post-monsoon
            default:
                return new BigDecimal("1.00"); // Winter
        }
    }

    /**
     * History multiplier based on AI risk score
     * Low risk score = discount | High risk score = surcharge
     */
    private BigDecimal calculateHistoryMultiplier(BigDecimal riskScore) {
        if (riskScore == null) return BigDecimal.ONE;
        double score = riskScore.doubleValue();
        if (score < 30)       return new BigDecimal("0.90"); // Very low risk, discount
        else if (score < 50)  return new BigDecimal("0.95"); // Low risk
        else if (score < 70)  return new BigDecimal("1.00"); // Average
        else if (score < 85)  return new BigDecimal("1.08"); // High risk
        else                  return new BigDecimal("1.15"); // Very high risk
    }

    private BigDecimal getBaseRate(Policy.PlanType planType) {
        return switch (planType) {
            case BASIC    -> BASE_BASIC;
            case STANDARD -> BASE_STANDARD;
            case PRO      -> BASE_PRO;
        };
    }

    private BigDecimal getCoverageAmount(Policy.PlanType planType) {
        return switch (planType) {
            case BASIC    -> COVERAGE_BASIC;
            case STANDARD -> COVERAGE_STANDARD;
            case PRO      -> COVERAGE_PRO;
        };
    }

    // Result DTO
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PremiumResult {
        private BigDecimal basePremium;
        private BigDecimal finalPremium;
        private BigDecimal coverageAmount;
        private BigDecimal zoneMultiplier;
        private BigDecimal seasonMultiplier;
        private BigDecimal historyMultiplier;
        private Policy.PlanType planType;
    }
}
