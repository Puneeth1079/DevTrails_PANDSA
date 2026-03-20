package com.gigshield.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WeatherApiService {

    @Value("${weather.api.key:demo}")
    private String apiKey;

    @Value("${weather.api.url:https://api.openweathermap.org/data/2.5/weather}")
    private String apiUrl;

    private final WebClient webClient = WebClient.create();

    public Map<String, Object> getCurrentConditions(String city) {
        try {
            if ("YOUR_OPENWEATHERMAP_API_KEY".equals(apiKey) || "demo".equals(apiKey)) {
                return getMockWeatherData(city);
            }
            // Real API call
            Map response = webClient.get()
                    .uri(apiUrl + "?q={city}&appid={key}&units=metric", city, apiKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return response != null ? response : getMockWeatherData(city);
        } catch (Exception e) {
            log.warn("Weather API error for {}: {} — using mock data", city, e.getMessage());
            return getMockWeatherData(city);
        }
    }

    private Map<String, Object> getMockWeatherData(String city) {
        Map<String, Object> data = new HashMap<>();
        // Simulate different conditions for different cities
        switch (city.toLowerCase()) {
            case "mumbai" -> {
                data.put("rainfall_mm", 18.5);      // Above 15mm threshold → HEAVY_RAIN
                data.put("feels_like_celsius", 34.0);
                data.put("description", "heavy intensity rain");
                data.put("city", city);
            }
            case "delhi" -> {
                data.put("rainfall_mm", 2.0);
                data.put("feels_like_celsius", 48.0);  // Above 46°C → EXTREME_HEAT
                data.put("description", "haze");
                data.put("city", city);
            }
            default -> {
                data.put("rainfall_mm", 3.0);
                data.put("feels_like_celsius", 32.0);
                data.put("description", "partly cloudy");
                data.put("city", city);
            }
        }
        return data;
    }

    public boolean isHeavyRain(Map<String, Object> weatherData) {
        Object rainfall = weatherData.get("rainfall_mm");
        if (rainfall == null) return false;
        return Double.parseDouble(rainfall.toString()) >= 15.0;
    }

    public boolean isExtremeHeat(Map<String, Object> weatherData) {
        Object temp = weatherData.get("feels_like_celsius");
        if (temp == null) return false;
        return Double.parseDouble(temp.toString()) >= 46.0;
    }
}
