package com.gigshield.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    private PlanType planType;

    @Column(name = "weekly_premium", nullable = false, precision = 8, scale = 2)
    private BigDecimal weeklyPremium;

    @Column(name = "base_premium", nullable = false, precision = 8, scale = 2)
    private BigDecimal basePremium;

    @Column(name = "coverage_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal coverageAmount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyStatus status = PolicyStatus.ACTIVE;

    @Column(name = "auto_renew")
    private Boolean autoRenew = true;

    @Column(name = "zone_multiplier", precision = 4, scale = 2)
    private BigDecimal zoneMultiplier = BigDecimal.ONE;

    @Column(name = "season_multiplier", precision = 4, scale = 2)
    private BigDecimal seasonMultiplier = BigDecimal.ONE;

    @Column(name = "history_multiplier", precision = 4, scale = 2)
    private BigDecimal historyMultiplier = BigDecimal.ONE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // Policy always runs for 7 days (weekly model)
        if (startDate != null && endDate == null) {
            endDate = startDate.plusDays(7);
        }
    }

    public enum PlanType { BASIC, STANDARD, PRO }
    public enum PolicyStatus { ACTIVE, PAUSED, EXPIRED, CANCELLED }
}
