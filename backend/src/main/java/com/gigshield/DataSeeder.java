package com.gigshield;

import com.gigshield.model.*;
import com.gigshield.model.enums.*;
import com.gigshield.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final PolicyRepository policyRepository;
    private final DisruptionEventRepository disruptionEventRepository;
    private final ClaimRepository claimRepository;
    private final PayoutRepository payoutRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("📦 DataSeeder: Database already seeded. Skipping...");
            return;
        }

        log.info("🌱 DataSeeder: Seeding initial data...");

        // ── Admin ──────────────────────────────────────────────────────────
        User admin = User.builder()
                .name("GigShield Admin")
                .mobile("9000000000")
                .email("admin@gigshield.in")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .role(Role.ADMIN)
                .isVerified(true)
                .isActive(true)
                .build();
        userRepository.save(admin);

        // ── Workers ────────────────────────────────────────────────────────
        String[][] workers = {
            {"9111111111", "Rahul Sharma", "rahul@example.com", "Mumbai", "Andheri West", "ZOMATO"},
            {"9111111112", "Priya Nair", "priya@example.com", "Delhi", "Karol Bagh", "SWIGGY"},
            {"9111111113", "Suresh Babu", "suresh@example.com", "Bengaluru", "Koramangala", "ZOMATO"},
            {"9111111114", "Lakshmi Devi", "lakshmi@example.com", "Chennai", "Anna Nagar", "SWIGGY"},
            {"9111111115", "Mohammad Ali", "ali@example.com", "Hyderabad", "Banjara Hills", "ZOMATO"}
        };

        BigDecimal[] earnings = {BigDecimal.valueOf(950), BigDecimal.valueOf(800),
                BigDecimal.valueOf(1100), BigDecimal.valueOf(700), BigDecimal.valueOf(1200)};

        WorkerProfile[] profiles = new WorkerProfile[5];
        for (int i = 0; i < workers.length; i++) {
            User u = User.builder()
                    .mobile(workers[i][0]).name(workers[i][1]).email(workers[i][2])
                    .passwordHash(passwordEncoder.encode("Worker@123"))
                    .role(Role.WORKER).isVerified(true).isActive(true).build();
            u = userRepository.save(u);

            WorkerProfile wp = WorkerProfile.builder()
                    .user(u)
                    .platform(workers[i][5])
                    .city(workers[i][3])
                    .zone(workers[i][4])
                    .avgDailyEarnings(earnings[i])
                    .avgDailyHours(BigDecimal.valueOf(8))
                    .upiId(workers[i][0] + "@upi")
                    .riskScore(BigDecimal.valueOf(40 + i * 8))
                    .build();
            profiles[i] = workerProfileRepository.save(wp);
        }

        // ── Policies ──────────────────────────────────────────────────────
        LocalDate today = LocalDate.now();
        CoverageTier[] tiers = {CoverageTier.BASIC, CoverageTier.STANDARD, CoverageTier.PREMIUM};
        BigDecimal[] premiums = {BigDecimal.valueOf(38), BigDecimal.valueOf(67), BigDecimal.valueOf(115)};
        BigDecimal[] payouts = {BigDecimal.valueOf(500), BigDecimal.valueOf(900), BigDecimal.valueOf(1500)};
        String[] triggers = {
            "[\"HEAVY_RAIN\",\"EXTREME_HEAT\"]",
            "[\"HEAVY_RAIN\",\"EXTREME_HEAT\",\"SEVERE_POLLUTION\",\"CURFEW\",\"FLOOD\"]",
            "[\"HEAVY_RAIN\",\"EXTREME_HEAT\",\"SEVERE_POLLUTION\",\"CURFEW\",\"FLOOD\"]"
        };

        Policy[] policies = new Policy[3];
        for (int i = 0; i < 3; i++) {
            Policy p = Policy.builder()
                    .worker(profiles[i])
                    .policyNumber("GS-2026-" + (10000001 + i))
                    .coverageTier(tiers[i])
                    .weeklyPremium(premiums[i])
                    .maxWeeklyPayout(payouts[i])
                    .startDate(today)
                    .endDate(today.plusDays(7))
                    .status(PolicyStatus.ACTIVE)
                    .autoRenew(i % 2 == 0)
                    .triggersCovered(triggers[i])
                    .build();
            policies[i] = policyRepository.save(p);
        }

        // ── Disruption Events ─────────────────────────────────────────────
        DisruptionEvent[] events = {
            disruptionEventRepository.save(DisruptionEvent.builder()
                .triggerType(TriggerType.HEAVY_RAIN).city("Mumbai").zone("Andheri West")
                .severityValue(BigDecimal.valueOf(18.5)).severityUnit("mm/hr")
                .thresholdBreached(BigDecimal.valueOf(15))
                .eventStart(LocalDateTime.now().minusHours(2))
                .dataSource("OpenWeatherMap").isActive(true).build()),

            disruptionEventRepository.save(DisruptionEvent.builder()
                .triggerType(TriggerType.SEVERE_POLLUTION).city("Delhi").zone("Karol Bagh")
                .severityValue(BigDecimal.valueOf(320)).severityUnit("AQI")
                .thresholdBreached(BigDecimal.valueOf(300))
                .eventStart(LocalDateTime.now().minusHours(5))
                .dataSource("IQAir").isActive(true).build()),

            disruptionEventRepository.save(DisruptionEvent.builder()
                .triggerType(TriggerType.CURFEW).city("Chennai").zone("Anna Nagar")
                .severityValue(BigDecimal.valueOf(1)).severityUnit("alert")
                .thresholdBreached(BigDecimal.valueOf(1))
                .eventStart(LocalDateTime.now().minusHours(3))
                .dataSource("MockCivicAlert").isActive(false).build())
        };

        // ── Claims ────────────────────────────────────────────────────────
        Claim[] claims = new Claim[3];
        for (int i = 0; i < 3; i++) {
            claims[i] = claimRepository.save(Claim.builder()
                .claimNumber("CLM-SEED-" + (100 + i))
                .policy(policies[i])
                .worker(profiles[i])
                .disruptionEvent(events[i])
                .triggerType(events[i].getTriggerType())
                .hoursLost(BigDecimal.valueOf(4))
                .payoutAmount(payouts[i].multiply(BigDecimal.valueOf(0.5)))
                .status(i < 2 ? ClaimStatus.PAID : ClaimStatus.PENDING_REVIEW)
                .fraudScore(BigDecimal.valueOf(i * 5))
                .fraudFlags("[]")
                .autoTriggered(true)
                .processedAt(LocalDateTime.now())
                .build());
        }

        // ── Payouts ───────────────────────────────────────────────────────
        for (int i = 0; i < 2; i++) {
            payoutRepository.save(Payout.builder()
                .claim(claims[i])
                .worker(profiles[i])
                .amount(claims[i].getPayoutAmount())
                .paymentMethod("UPI")
                .paymentReference("pout_mock_SEED" + (i + 1))
                .gatewayResponse("{\"status\":\"processed\",\"utr\":\"UTR_SEED_" + i + "\"}")
                .status(PayoutStatus.SUCCESS)
                .initiatedAt(LocalDateTime.now().minusHours(1))
                .completedAt(LocalDateTime.now())
                .build());
        }

        log.info("✅ DataSeeder: Completed — 1 admin, {} workers, 3 policies, 3 events, 3 claims, 2 payouts",
                workers.length);
        log.info("🔑 Admin login: mobile=9000000000, password=Admin@123");
        log.info("🔑 Worker login: mobile=9111111111, password=Worker@123");
    }
}
