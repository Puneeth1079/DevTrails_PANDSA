package com.gigshield.model;

import com.gigshield.model.enums.CoverageTier;
import com.gigshield.model.enums.PolicyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "policies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private WorkerProfile worker;

    @Column(name = "policy_number", unique = true, nullable = false, length = 30)
    private String policyNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "coverage_tier", nullable = false)
    private CoverageTier coverageTier;

    @Column(name = "weekly_premium", nullable = false, precision = 8, scale = 2)
    private BigDecimal weeklyPremium;

    @Column(name = "max_weekly_payout", nullable = false, precision = 8, scale = 2)
    private BigDecimal maxWeeklyPayout;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PolicyStatus status = PolicyStatus.PENDING;

    @Column(name = "auto_renew")
    @Builder.Default
    private Boolean autoRenew = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "triggers_covered", columnDefinition = "json")
    private String triggersCovered;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Claim> claims;
}
