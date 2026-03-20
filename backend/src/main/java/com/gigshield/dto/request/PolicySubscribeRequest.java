package com.gigshield.dto.request;

import com.gigshield.model.enums.CoverageTier;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PolicySubscribeRequest {

    @NotNull(message = "Coverage tier is required")
    private CoverageTier coverageTier;

    private LocalDate startDate;

    private boolean autoRenew;

    // Optional list of trigger types sent by frontend (ignored here, derived from tier server-side)
    private List<String> triggersCovered;

    // Return today's date if startDate not provided
    public LocalDate getStartDate() {
        return startDate != null ? startDate : LocalDate.now();
    }
}
