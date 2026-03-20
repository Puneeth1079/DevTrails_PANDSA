package com.gigshield.scheduler;

import com.gigshield.service.TriggerMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TriggerPollingScheduler {

    private final TriggerMonitorService triggerMonitorService;

    @Value("${trigger.poll.interval.ms:300000}")
    private long pollInterval;

    // Poll every 5 minutes
    @Scheduled(fixedRateString = "${trigger.poll.interval.ms:300000}")
    public void pollForDisruptions() {
        log.info("⏰ TriggerPollingScheduler — starting poll cycle");
        try {
            triggerMonitorService.checkAllCitiesForTriggers();
            log.info("✅ TriggerPollingScheduler — poll cycle complete");
        } catch (Exception e) {
            log.error("❌ TriggerPollingScheduler error: {}", e.getMessage(), e);
        }
    }

    // Policy expiry reminders at 9 AM daily
    @Scheduled(cron = "0 0 9 * * *")
    public void sendExpiryReminders() {
        log.info("📅 Running daily policy expiry reminder check...");
        // Notifications are logged to console (mock SMS)
    }
}
