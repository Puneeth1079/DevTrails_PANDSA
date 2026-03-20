package com.gigshield.dto.request;

import com.gigshield.model.enums.TriggerType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ClaimRequest {
    private Long policyId;
    private TriggerType triggerType;
    private BigDecimal hoursLost;
    private BigDecimal workerLocationLat;
    private BigDecimal workerLocationLng;
    private String notes;
}
