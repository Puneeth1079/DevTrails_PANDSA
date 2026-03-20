package com.gigshield.controller;

import com.gigshield.dto.response.ApiResponse;
import com.gigshield.dto.response.ClaimResponse;
import com.gigshield.model.Claim;
import com.gigshield.model.WorkerProfile;
import com.gigshield.model.enums.ClaimStatus;
import com.gigshield.security.JwtTokenProvider;
import com.gigshield.service.ClaimService;
import com.gigshield.service.WorkerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Tag(name = "Claims", description = "Claims management")
public class ClaimController {

    private final ClaimService claimService;
    private final WorkerService workerService;
    private final JwtTokenProvider tokenProvider;

    @GetMapping("/my-claims")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<ApiResponse<Page<ClaimResponse>>> getMyClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        WorkerProfile worker = workerService.getWorkerByUserId(userId);
        Page<ClaimResponse> claims = claimService.getWorkerClaims(worker.getId(), PageRequest.of(page, size))
                .map(claimService::toClaimResponse);
        return ResponseEntity.ok(ApiResponse.success(claims));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ClaimResponse>>> getAllClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ClaimStatus status) {
        Page<ClaimResponse> claims = claimService.getAllClaims(status, PageRequest.of(page, size))
                .map(claimService::toClaimResponse);
        return ResponseEntity.ok(ApiResponse.success(claims));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClaimResponse>> approveClaim(
            @PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        Claim claim = claimService.updateClaimStatus(id, ClaimStatus.APPROVED, notes);
        return ResponseEntity.ok(ApiResponse.success(claimService.toClaimResponse(claim), "Claim approved"));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClaimResponse>> rejectClaim(
            @PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : "Rejected by admin";
        Claim claim = claimService.updateClaimStatus(id, ClaimStatus.REJECTED, notes);
        return ResponseEntity.ok(ApiResponse.success(claimService.toClaimResponse(claim), "Claim rejected"));
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return tokenProvider.getUserIdFromToken(token);
    }
}
