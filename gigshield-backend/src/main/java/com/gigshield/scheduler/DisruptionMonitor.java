package com.gigshield.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * DisruptionMonitor - Scheduled Job
 *
 * Runs every 15 minutes to:
 * 1. Poll OpenWeatherMap API for rainfall, temperature
 * 2. Poll OpenAQ for AQI levels
 * 3. Poll mock news/traffic API for bandh/curfew alerts
 * 4. Evaluate each trigger threshold
 * 5. Auto-generate claims for active policy holders in affected zones
 */
@Component
public class DisruptionMonitor {

    private static final Logger log = LoggerFactory.getLogger(DisruptionMonitor.class);

    // Trigger thresholds
    private static final double RAIN_THRESHOLD_MM     = 35.0;  // mm in 3 hrs
    private static final double HEAT_THRESHOLD_CELSIUS = 42.0;
    private static final int    AQI_THRESHOLD          = 300;

    /**
     * Runs every 15 minutes (900,000 ms)
     * Checks all monitored zones for active disruptions
     */
    @Scheduled(fixedRateString = "${app.scheduler.interval}")
    public void monitorDisruptions() {
        log.info("=== GigShield Disruption Monitor Running ===");

        checkRainfallTrigger();
        checkHeatTrigger();
        checkAQITrigger();
        checkBandhCurfewTrigger();

        log.info("=== Disruption Monitor Cycle Complete ===");
    }

    /**
     * TRIGGER 1: Heavy Rain
     * Source: OpenWeatherMap API - rain.3h field
     * Threshold: > 35mm in 3 hours
     */
    private void checkRainfallTrigger() {
        log.info("Checking rainfall levels...");
        // TODO Phase 2:
        // 1. Call OpenWeatherMap API for each monitored zone/city
        // 2. Extract rain.3h value from response
        // 3. If rain.3h > RAIN_THRESHOLD_MM:
        //    a. Save DisruptionEvent to DB
        //    b. Find all ACTIVE policies in that zone
        //    c. Auto-generate Claims for each worker
        //    d. Run fraud checks
        //    e. Process payouts for approved claims
    }

    /**
     * TRIGGER 2: Extreme Heat
     * Source: OpenWeatherMap API - main.temp field
     * Threshold: > 42 degrees C between 11AM - 4PM
     */
    private void checkHeatTrigger() {
        log.info("Checking temperature levels...");
        // TODO Phase 2: Same pattern as rainfall
    }

    /**
     * TRIGGER 3: Severe AQI
     * Source: OpenAQ API
     * Threshold: AQI > 300 sustained for 4+ hours
     */
    private void checkAQITrigger() {
        log.info("Checking AQI levels...");
        // TODO Phase 2: Call OpenAQ API
    }

    /**
     * TRIGGER 4: Flood Alert
     * Source: IMD Mock API / Government disaster alert
     * Threshold: Official flood alert for pincode
     */
    private void checkFloodTrigger() {
        log.info("Checking flood alerts...");
        // TODO Phase 2: Call IMD mock API
    }

    /**
     * TRIGGER 5: Bandh / Curfew
     * Source: News API / Mock civic alert system
     * Threshold: Declared bandh or curfew in city
     */
    private void checkBandhCurfewTrigger() {
        log.info("Checking bandh/curfew alerts...");
        // TODO Phase 2: Call mock news API or civic alert system
    }
}
