package com.gigshield.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AqiApiService {

    @Value("${aqi.api.key:demo}")
    private String apiKey;

    private final WebClient webClient = WebClient.create();

    public Map<String, Object> getCurrentAqi(String city) {
        try {
            if ("YOUR_IQAIR_KEY".equals(apiKey) || "demo".equals(apiKey)) {
                return getMockAqiData(city);
            }
            // Real API would go here
            return getMockAqiData(city);
        } catch (Exception e) {
            log.warn("AQI API error for {}: {} — using mock", city, e.getMessage());
            return getMockAqiData(city);
        }
    }

    private Map<String, Object> getMockAqiData(String city) {
        Map<String, Object> data = new HashMap<>();
        // Delhi typically has severe pollution
        int aqi = switch (city.toLowerCase()) {
            case "delhi" -> 320;     // Above 300 → SEVERE_POLLUTION
            case "kolkata" -> 210;
            case "mumbai" -> 150;
            default -> 80;
        };
        data.put("aqi", aqi);
        data.put("city", city);
        data.put("pollutant", "PM2.5");
        return data;
    }

    public boolean isSeverePollution(Map<String, Object> aqiData) {
        Object aqi = aqiData.get("aqi");
        if (aqi == null) return false;
        return Integer.parseInt(aqi.toString()) >= 300;
    }
}
