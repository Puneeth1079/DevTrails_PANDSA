package com.gigshield.controller;

import com.gigshield.dto.response.ApiResponse;
import com.gigshield.model.DisruptionEvent;
import com.gigshield.model.enums.TriggerType;
import com.gigshield.repository.DisruptionEventRepository;
import com.gigshield.service.TriggerMonitorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/triggers")
@RequiredArgsConstructor
@Tag(name = "Triggers", description = "Disruption trigger management (Admin)")
public class TriggerController {

    private final TriggerMonitorService triggerMonitorService;
    private final DisruptionEventRepository disruptionEventRepository;

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DisruptionEvent>>> getActiveEvents() {
        return ResponseEntity.ok(ApiResponse.success(disruptionEventRepository.findByIsActiveTrue()));
    }

    @PostMapping("/simulate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DisruptionEvent>> simulateTrigger(@RequestBody Map<String, Object> req) {
        String city = (String) req.get("city");
        TriggerType type = TriggerType.valueOf((String) req.get("triggerType"));
        BigDecimal severity = new BigDecimal(req.get("severityValue").toString());
        String unit = (String) req.getOrDefault("severityUnit", "units");
        BigDecimal threshold = new BigDecimal(req.getOrDefault("threshold", req.get("severityValue")).toString());

        DisruptionEvent event = triggerMonitorService.createDisruptionEvent(city, type, severity, unit, threshold, "MANUAL_SIMULATION");
        return ResponseEntity.ok(ApiResponse.success(event, "Trigger simulated for " + city));
    }

    @PostMapping("/poll-now")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> pollNow() {
        triggerMonitorService.checkAllCitiesForTriggers();
        return ResponseEntity.ok(ApiResponse.success("Poll completed", "Trigger polling executed manually"));
    }
}
