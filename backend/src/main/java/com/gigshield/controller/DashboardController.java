package com.gigshield.controller;

import com.gigshield.dto.response.ApiResponse;
import com.gigshield.dto.response.DashboardAdminResponse;
import com.gigshield.dto.response.DashboardWorkerResponse;
import com.gigshield.model.WorkerProfile;
import com.gigshield.security.JwtTokenProvider;
import com.gigshield.service.DashboardService;
import com.gigshield.service.WorkerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Worker and admin dashboards")
public class DashboardController {

    private final DashboardService dashboardService;
    private final WorkerService workerService;
    private final JwtTokenProvider tokenProvider;

    @GetMapping("/worker")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<ApiResponse<DashboardWorkerResponse>> getWorkerDashboard(HttpServletRequest request) {
        Long userId = getUserId(request);
        WorkerProfile worker = workerService.getWorkerByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getWorkerDashboard(worker.getId())));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardAdminResponse>> getAdminDashboard() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getAdminDashboard()));
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return tokenProvider.getUserIdFromToken(token);
    }
}
