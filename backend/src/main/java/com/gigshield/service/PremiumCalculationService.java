package com.gigshield.service;

import com.gigshield.model.enums.CoverageTier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.time.MonthDay;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PremiumCalculationService {

    @Value("${ml.service.url:http://localhost:8000}")
    private String mlServiceUrl;

    private static final Map<CoverageTier, BigDecimal> BASE_PREMIUMS = Map.of(
            CoverageTier.BASIC, BigDecimal.valueOf(35),
            CoverageTier.STANDARD, BigDecimal.valueOf(65),
            CoverageTier.PREMIUM, BigDecimal.valueOf(109)
    );

    private static final Map<CoverageTier, BigDecimal> MAX_PAYOUTS = Map.of(
            CoverageTier.BASIC, BigDecimal.valueOf(500),
            CoverageTier.STANDARD, BigDecimal.valueOf(900),
            CoverageTier.PREMIUM, BigDecimal.valueOf(1500)
    );

    public BigDecimal calculateWeeklyPremium(CoverageTier tier, String city, String zone,
                                              BigDecimal avgDailyEarnings, String platform,
                                              int renewalCount, boolean autoRenew) {
        try {
            return calculateWithLocalModel(tier, city, zone, avgDailyEarnings, platform, renewalCount, autoRenew);
        } catch (Exception e) {
            log.warn("ML service unavailable, using local calculation: {}", e.getMessage());
            return calculateWithLocalModel(tier, city, zone, avgDailyEarnings, platform, renewalCount, autoRenew);
        }
    }

    private BigDecimal calculateWithLocalModel(CoverageTier tier, String city, String zone,
                                                BigDecimal avgDailyEarnings, String platform,
                                                int renewalCount, boolean autoRenew) {
        BigDecimal base = BASE_PREMIUMS.get(tier);

        // City risk factor
        double cityFactor = getCityFactor(city);

        // Season factor
        double seasonFactor = getSeasonFactor();

        // Zone risk addition
        double zoneRisk = getZoneRisk(zone);

        // Loyalty discount
        double loyaltyDiscount = renewalCount > 2 ? 5.0 : 0.0;

        // Platform bonus
        double platformDiscount = ("ZOMATO".equalsIgnoreCase(platform) || "SWIGGY".equalsIgnoreCase(platform)) ? 2.0 : 0.0;

        // Auto-renew discount
        double autoRenewDiscount = autoRenew ? base.doubleValue() * 0.10 : 0.0;

        double premium = base.doubleValue() * cityFactor * seasonFactor + zoneRisk - loyaltyDiscount - platformDiscount - autoRenewDiscount;
        premium = Math.max(BASE_PREMIUMS.get(tier).doubleValue() * 0.8, premium); // min 80% of base

        return BigDecimal.valueOf(premium).setScale(0, RoundingMode.HALF_UP);
    }

    private double getCityFactor(String city) {
        if (city == null) return 1.0;
        return switch (city.toLowerCase()) {
            case "mumbai", "delhi", "chennai" -> 1.2;
            case "bengaluru", "hyderabad", "kolkata" -> 1.1;
            case "pune", "ahmedabad" -> 1.0;
            default -> 0.85;
        };
    }

    private double getSeasonFactor() {
        int month = java.time.LocalDate.now().getMonthValue();
        // Monsoon: June–September
        if (month >= 6 && month <= 9) return 1.15;
        // Winter: November–February
        if (month >= 11 || month <= 2) return 0.95;
        return 1.0;
    }

    private double getZoneRisk(String zone) {
        if (zone == null) return 0.0;
        // High-risk zones (flood-prone areas)
        String lowerZone = zone.toLowerCase();
        if (lowerZone.contains("dharavi") || lowerZone.contains("kurla") ||
            lowerZone.contains("mankhurd") || lowerZone.contains("lower parel")) {
            return 12.0;
        }
        if (lowerZone.contains("andheri") || lowerZone.contains("bandra") ||
            lowerZone.contains("sion")) {
            return 8.0;
        }
        return 0.0;
    }

    public BigDecimal getMaxWeeklyPayout(CoverageTier tier) {
        return MAX_PAYOUTS.get(tier);
    }

    public BigDecimal calculateClaimPayout(BigDecimal maxPayout, BigDecimal hoursLost, BigDecimal avgDailyHours) {
        if (avgDailyHours == null || avgDailyHours.compareTo(BigDecimal.ZERO) == 0) {
            avgDailyHours = BigDecimal.valueOf(8);
        }
        // Payout proportional to hours lost out of daily working hours
        BigDecimal ratio = hoursLost.divide(avgDailyHours, 4, RoundingMode.HALF_UP);
        return maxPayout.multiply(ratio).min(maxPayout).setScale(2, RoundingMode.HALF_UP);
    }
}
