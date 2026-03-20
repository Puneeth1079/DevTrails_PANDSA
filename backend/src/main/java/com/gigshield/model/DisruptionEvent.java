package com.gigshield.model;

import com.gigshield.model.enums.TriggerType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "disruption_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisruptionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    private TriggerType triggerType;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String zone;

    @Column(length = 10)
    private String pincode;

    @Column(name = "severity_value", precision = 8, scale = 2)
    private BigDecimal severityValue;

    @Column(name = "severity_unit", length = 20)
    private String severityUnit;

    @Column(name = "threshold_breached", precision = 8, scale = 2)
    private BigDecimal thresholdBreached;

    @Column(name = "event_start", nullable = false)
    private LocalDateTime eventStart;

    @Column(name = "event_end")
    private LocalDateTime eventEnd;

    @Column(name = "data_source", length = 100)
    private String dataSource;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_api_response", columnDefinition = "json")
    private String rawApiResponse;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
