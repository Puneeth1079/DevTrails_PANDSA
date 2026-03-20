package com.gigshield.controller;

import com.gigshield.dto.response.ApiResponse;
import com.gigshield.dto.response.WorkerProfileResponse;
import com.gigshield.model.WorkerProfile;
import com.gigshield.service.WorkerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin management endpoints")
public class AdminController {

    private final WorkerService workerService;

    @GetMapping("/workers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<WorkerProfile>>> getAllWorkers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String platform) {
        Page<WorkerProfile> workers = workerService.getAllWorkers(city, platform, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(workers));
    }

    @GetMapping("/workers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WorkerProfile>> getWorker(@PathVariable Long id) {
        WorkerProfile worker = workerService.getWorkerById(id);
        return ResponseEntity.ok(ApiResponse.success(worker));
    }
}
