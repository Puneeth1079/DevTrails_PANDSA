package com.gigshield.dto.response;

import com.gigshield.model.enums.ClaimStatus;
import com.gigshield.model.enums.TriggerType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ClaimResponse {
    private Long id;
    private String claimNumber;
    private String policyNumber;
    private TriggerType triggerType;
    private BigDecimal hoursLost;
    private BigDecimal payoutAmount;
    private ClaimStatus status;
    private BigDecimal fraudScore;
    private String fraudFlags;
    private Boolean autoTriggered;
    private String notes;
    private LocalDateTime claimedAt;
    private LocalDateTime processedAt;
    private Long workerId;
    private String workerName;
    private String city;
    private String disruptionEventId;
    private BigDecimal severityValue;
    private String severityUnit;
}
