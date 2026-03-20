package com.gigshield.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    // Scheduling is enabled via @EnableScheduling
    // TriggerPollingScheduler runs every 5 minutes
}
