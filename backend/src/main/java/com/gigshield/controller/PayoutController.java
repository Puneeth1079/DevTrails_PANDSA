package com.gigshield.controller;

import com.gigshield.dto.response.ApiResponse;
import com.gigshield.dto.response.PayoutResponse;
import com.gigshield.model.Payout;
import com.gigshield.repository.PayoutRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payouts")
@RequiredArgsConstructor
@Tag(name = "Payouts", description = "Payout management")
public class PayoutController {

    private final PayoutRepository payoutRepository;

    @GetMapping("/my-payouts")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<ApiResponse<List<PayoutResponse>>> getMyPayouts(
            @RequestParam Long workerId) {
        List<PayoutResponse> payouts = payoutRepository.findByWorkerId(workerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(payouts));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Payout>>> getAllPayouts() {
        return ResponseEntity.ok(ApiResponse.success(payoutRepository.findAll()));
    }

    private PayoutResponse toResponse(Payout p) {
        return PayoutResponse.builder()
                .id(p.getId())
                .claimId(p.getClaim() != null ? p.getClaim().getId() : null)
                .claimNumber(p.getClaim() != null ? p.getClaim().getClaimNumber() : null)
                .amount(p.getAmount())
                .paymentMethod(p.getPaymentMethod())
                .paymentReference(p.getPaymentReference())
                .status(p.getStatus())
                .initiatedAt(p.getInitiatedAt())
                .completedAt(p.getCompletedAt())
                .workerId(p.getWorker().getId())
                .workerName(p.getWorker().getUser().getName())
                .upiId(p.getWorker().getUpiId())
                .build();
    }
}
