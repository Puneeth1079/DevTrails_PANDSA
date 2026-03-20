package com.gigshield.controller;

import com.gigshield.dto.response.ApiResponse;
import com.gigshield.dto.response.WorkerProfileResponse;
import com.gigshield.model.WorkerProfile;
import com.gigshield.repository.UserRepository;
import com.gigshield.security.JwtTokenProvider;
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
@RequestMapping("/api/worker")
@RequiredArgsConstructor
@Tag(name = "Worker", description = "Worker profile management")
public class WorkerController {

    private final WorkerService workerService;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<ApiResponse<WorkerProfileResponse>> getMyProfile(HttpServletRequest request) {
        Long userId = getUserId(request);
        WorkerProfile worker = workerService.getWorkerByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(toResponse(worker)));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<ApiResponse<WorkerProfileResponse>> updateProfile(
            @RequestBody WorkerProfile updates, HttpServletRequest request) {
        Long userId = getUserId(request);
        WorkerProfile worker = workerService.getWorkerByUserId(userId);
        WorkerProfile updated = workerService.updateWorkerProfile(worker.getId(), updates);
        return ResponseEntity.ok(ApiResponse.success(toResponse(updated), "Profile updated"));
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<ApiResponse<Object>> getAlerts(HttpServletRequest request) {
        Long userId = getUserId(request);
        WorkerProfile worker = workerService.getWorkerByUserId(userId);
        // Return current alerts for worker's city — used by polling
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("city", worker.getCity(), "timestamp", System.currentTimeMillis())
        ));
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return tokenProvider.getUserIdFromToken(token);
    }

    private WorkerProfileResponse toResponse(WorkerProfile worker) {
        return WorkerProfileResponse.builder()
                .id(worker.getId())
                .userId(worker.getUser().getId())
                .name(worker.getUser().getName())
                .mobile(worker.getUser().getMobile())
                .email(worker.getUser().getEmail())
                .platform(worker.getPlatform())
                .platformPartnerId(worker.getPlatformPartnerId())
                .city(worker.getCity())
                .zone(worker.getZone())
                .pincode(worker.getPincode())
                .avgDailyEarnings(worker.getAvgDailyEarnings())
                .avgDailyHours(worker.getAvgDailyHours())
                .upiId(worker.getUpiId())
                .bankAccount(worker.getBankAccount())
                .ifsc(worker.getIfsc())
                .riskScore(worker.getRiskScore())
                .build();
    }
}
