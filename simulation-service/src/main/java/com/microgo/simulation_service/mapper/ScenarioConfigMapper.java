package com.microgo.simulation_service.mapper;

import com.microgo.simulation_service.enums.ScenarioType;
import com.microgo.simulation_service.enums.TrafficLevel;
import com.microgo.simulation_service.enums.WeatherCondition;
import com.microgo.simulation_service.enums.ZoneId;
import com.microgo.simulation_service.entity.ScenarioConfigEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public final class ScenarioConfigMapper {

    private ScenarioConfigMapper() {
        // Private constructor to prevent instantiation
    }

    public static ScenarioConfigEntity defaultConcertRainConfig(Instant now) {
        return ScenarioConfigEntity.builder()
                .id(UUID.randomUUID())
                .scenarioName(ScenarioType.CONCERT_RAIN)
                .city("London")
                .anchorZone(ZoneId.WEMBLEY_EVENT_ZONE)
                .startTime(LocalDateTime.of(2026, 6, 18, 23, 30))
                .weather(WeatherCondition.RAIN)
                .trafficLevel(TrafficLevel.HIGH)
                .passengerDemandMultiplier(2.2)
                .cancellationRiskMultiplier(1.4)
                .driverSpeedMultiplier(0.72)
                .airportFareBias(1.0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static ScenarioConfigEntity defaultAirportRushConfig(Instant now) {
        return ScenarioConfigEntity.builder()
                .id(UUID.randomUUID())
                .scenarioName(ScenarioType.AIRPORT_RUSH)
                .city("London")
                .anchorZone(ZoneId.CENTRAL_LONDON)
                .startTime(LocalDateTime.of(2026, 6, 18, 7, 30))
                .weather(WeatherCondition.CLEAR)
                .trafficLevel(TrafficLevel.HIGH)
                .passengerDemandMultiplier(1.9)
                .cancellationRiskMultiplier(1.15)
                .driverSpeedMultiplier(0.80)
                .airportFareBias(1.25)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
