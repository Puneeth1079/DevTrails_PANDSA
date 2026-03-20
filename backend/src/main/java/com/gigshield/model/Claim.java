package com.gigshield.model;

import com.gigshield.model.enums.ClaimStatus;
import com.gigshield.model.enums.TriggerType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "claims")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_number", unique = true, nullable = false, length = 30)
    private String claimNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private WorkerProfile worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disruption_event_id")
    private DisruptionEvent disruptionEvent;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    private TriggerType triggerType;

    @Column(name = "hours_lost", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal hoursLost = BigDecimal.ZERO;

    @Column(name = "payout_amount", nullable = false, precision = 8, scale = 2)
    private BigDecimal payoutAmount;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ClaimStatus status = ClaimStatus.AUTO_APPROVED;

    @Column(name = "fraud_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal fraudScore = BigDecimal.ZERO;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "fraud_flags", columnDefinition = "json")
    private String fraudFlags;

    @Column(name = "auto_triggered")
    @Builder.Default
    private Boolean autoTriggered = true;

    @Column(name = "worker_location_lat", precision = 10, scale = 7)
    private BigDecimal workerLocationLat;

    @Column(name = "worker_location_lng", precision = 10, scale = 7)
    private BigDecimal workerLocationLng;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "claimed_at", updatable = false)
    private LocalDateTime claimedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @OneToOne(mappedBy = "claim", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payout payout;
}
