package com.gigshield.dto.response;

import com.gigshield.model.enums.PayoutStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PayoutResponse {
    private Long id;
    private Long claimId;
    private String claimNumber;
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentReference;
    private PayoutStatus status;
    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;
    private Long workerId;
    private String workerName;
    private String upiId;
}
