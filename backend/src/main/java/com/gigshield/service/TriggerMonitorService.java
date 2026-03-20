package com.gigshield.service;

import com.gigshield.model.DisruptionEvent;
import com.gigshield.model.Policy;
import com.gigshield.model.enums.TriggerType;
import com.gigshield.repository.DisruptionEventRepository;
import com.gigshield.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TriggerMonitorService {

    private final WeatherApiService weatherApiService;
    private final AqiApiService aqiApiService;
    private final DisruptionEventRepository disruptionEventRepository;
    private final PolicyRepository policyRepository;
    private final ClaimService claimService;

    private static final List<String> MONITORED_CITIES =
            List.of("Mumbai", "Delhi", "Bengaluru", "Chennai", "Hyderabad", "Pune", "Kolkata", "Ahmedabad");

    public void checkAllCitiesForTriggers() {
        log.info("🔍 Trigger monitor polling {} cities...", MONITORED_CITIES.size());
        for (String city : MONITORED_CITIES) {
            try {
                checkCityForWeatherTriggers(city);
                checkCityForAqiTriggers(city);
                checkCityForMockCivicAlerts(city);
            } catch (Exception e) {
                log.error("Error checking triggers for {}: {}", city, e.getMessage());
            }
        }
    }

    private void checkCityForWeatherTriggers(String city) {
        Map<String, Object> weather = weatherApiService.getCurrentConditions(city);

        if (weatherApiService.isHeavyRain(weather)) {
            double rainMm = Double.parseDouble(weather.get("rainfall_mm").toString());
            // Check if event already active
            List<DisruptionEvent> existing = disruptionEventRepository
                    .findByCityAndTriggerTypeAndIsActiveTrue(city, TriggerType.HEAVY_RAIN);
            if (existing.isEmpty()) {
                DisruptionEvent event = createDisruptionEvent(city, TriggerType.HEAVY_RAIN,
                        BigDecimal.valueOf(rainMm), "mm/hr", BigDecimal.valueOf(15), "OpenWeatherMap");
                triggerClaimsForCity(city, event, BigDecimal.valueOf(4));
            }
        }

        if (weatherApiService.isExtremeHeat(weather)) {
            double feelsLike = Double.parseDouble(weather.get("feels_like_celsius").toString());
            List<DisruptionEvent> existing = disruptionEventRepository
                    .findByCityAndTriggerTypeAndIsActiveTrue(city, TriggerType.EXTREME_HEAT);
            if (existing.isEmpty()) {
                DisruptionEvent event = createDisruptionEvent(city, TriggerType.EXTREME_HEAT,
                        BigDecimal.valueOf(feelsLike), "°C", BigDecimal.valueOf(46), "OpenWeatherMap");
                triggerClaimsForCity(city, event, BigDecimal.valueOf(3));
            }
        }
    }

    private void checkCityForAqiTriggers(String city) {
        Map<String, Object> aqiData = aqiApiService.getCurrentAqi(city);
        if (aqiApiService.isSeverePollution(aqiData)) {
            int aqi = Integer.parseInt(aqiData.get("aqi").toString());
            List<DisruptionEvent> existing = disruptionEventRepository
                    .findByCityAndTriggerTypeAndIsActiveTrue(city, TriggerType.SEVERE_POLLUTION);
            if (existing.isEmpty()) {
                DisruptionEvent event = createDisruptionEvent(city, TriggerType.SEVERE_POLLUTION,
                        BigDecimal.valueOf(aqi), "AQI", BigDecimal.valueOf(300), "IQAir");
                triggerClaimsForCity(city, event, BigDecimal.valueOf(5));
            }
        }
    }

    private void checkCityForMockCivicAlerts(String city) {
        // Mock civic alert — in production, call actual civic API
        // For demo, we simulate a curfew in Chennai on specific conditions
        if ("Chennai".equalsIgnoreCase(city) && isMockCurfewActive(city)) {
            List<DisruptionEvent> existing = disruptionEventRepository
                    .findByCityAndTriggerTypeAndIsActiveTrue(city, TriggerType.CURFEW);
            if (existing.isEmpty()) {
                DisruptionEvent event = createDisruptionEvent(city, TriggerType.CURFEW,
                        BigDecimal.valueOf(1), "alert", BigDecimal.valueOf(1), "MockCivicAlert");
                triggerClaimsForCity(city, event, BigDecimal.valueOf(6));
            }
        }
    }

    public DisruptionEvent createDisruptionEvent(String city, TriggerType type,
                                                  BigDecimal severityValue, String unit,
                                                  BigDecimal threshold, String source) {
        DisruptionEvent event = DisruptionEvent.builder()
                .triggerType(type)
                .city(city)
                .severityValue(severityValue)
                .severityUnit(unit)
                .thresholdBreached(threshold)
                .eventStart(LocalDateTime.now())
                .dataSource(source)
                .isActive(true)
                .build();
        event = disruptionEventRepository.save(event);
        log.info("⚡ DisruptionEvent created: {} in {} (severity={} {})", type, city, severityValue, unit);
        return event;
    }

    private void triggerClaimsForCity(String city, DisruptionEvent event, BigDecimal hoursLost) {
        List<Policy> activePolicies = policyRepository.findActivePoliciesByCity(city);
        log.info("🔔 Triggering claims for {} active policies in {}", activePolicies.size(), city);
        for (Policy policy : activePolicies) {
            try {
                String triggersCovered = policy.getTriggersCovered();
                if (triggersCovered != null && triggersCovered.contains(event.getTriggerType().name())) {
                    claimService.autoTriggerClaim(policy, event, hoursLost);
                }
            } catch (Exception e) {
                log.error("Failed to trigger claim for policy {}: {}", policy.getPolicyNumber(), e.getMessage());
            }
        }
    }

    private boolean isMockCurfewActive(String city) {
        // For hackathon demo: always false unless manually simulated
        return false;
    }
}
