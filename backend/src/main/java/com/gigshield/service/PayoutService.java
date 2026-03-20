package com.gigshield.service;

import com.gigshield.model.Claim;
import com.gigshield.model.Payout;
import com.gigshield.model.WorkerProfile;
import com.gigshield.model.enums.ClaimStatus;
import com.gigshield.model.enums.PayoutStatus;
import com.gigshield.repository.PayoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayoutService {

    private final PayoutRepository payoutRepository;

    @Transactional
    public Payout initiateAutoPayout(Claim claim) {
        // Check if payout already exists
        if (payoutRepository.findByClaimId(claim.getId()).isPresent()) {
            return payoutRepository.findByClaimId(claim.getId()).get();
        }

        WorkerProfile worker = claim.getWorker();
        String upiId = worker.getUpiId();

        Payout payout = Payout.builder()
                .claim(claim)
                .worker(worker)
                .amount(claim.getPayoutAmount())
                .paymentMethod("UPI")
                .status(PayoutStatus.PROCESSING)
                .initiatedAt(LocalDateTime.now())
                .build();

        payout = payoutRepository.save(payout);

        // Simulate Razorpay/UPI payout (90% success rate for demo realism)
        boolean success = new Random().nextInt(10) < 9;

        if (success) {
            String mockRef = "pout_mock_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String utr = "UTR_MOCK_" + System.currentTimeMillis();
            long amountInPaise = claim.getPayoutAmount().multiply(BigDecimal.valueOf(100)).longValue();

            String gatewayResponse = String.format(
                "{\"id\":\"%s\",\"status\":\"processed\",\"utr\":\"%s\",\"amount\":%d,\"upi_id\":\"%s\",\"mode\":\"UPI\"}",
                mockRef, utr, amountInPaise, upiId != null ? upiId : "mock@upi"
            );

            payout.setPaymentReference(mockRef);
            payout.setGatewayResponse(gatewayResponse);
            payout.setStatus(PayoutStatus.SUCCESS);
            payout.setCompletedAt(LocalDateTime.now());

            log.info("✅ Payout SUCCESS: ₹{} to {} | ref={} | claim={}",
                    claim.getPayoutAmount(), upiId, mockRef, claim.getClaimNumber());
        } else {
            payout.setStatus(PayoutStatus.FAILED);
            payout.setGatewayResponse("{\"error\":\"Gateway timeout — retry scheduled\"}");
            log.warn("⚠️ Payout FAILED for claim {} — will retry", claim.getClaimNumber());
        }

        return payoutRepository.save(payout);
    }

    public BigDecimal getTotalPayoutsThisMonth() {
        BigDecimal total = payoutRepository.sumPayoutsThisMonth();
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalPayoutForWorker(Long workerId) {
        BigDecimal total = payoutRepository.sumSuccessfulPayoutsByWorker(workerId);
        return total != null ? total : BigDecimal.ZERO;
    }
}
