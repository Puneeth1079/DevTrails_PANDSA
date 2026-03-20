package com.gigshield.dto.request;

import lombok.Data;

@Data
public class PayoutRequest {
    private Long claimId;
    private String paymentMethod;  // UPI, BANK_TRANSFER, WALLET
}
