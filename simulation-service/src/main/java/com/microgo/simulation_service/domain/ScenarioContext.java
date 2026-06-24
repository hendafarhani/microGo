package com.microgo.simulation_service.domain;

import com.microgo.simulation_service.enums.ScenarioType;
import com.microgo.simulation_service.enums.TrafficLevel;
import com.microgo.simulation_service.enums.WeatherCondition;
import com.microgo.simulation_service.enums.ZoneId;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class ScenarioContext {
    UUID simulationRunId;
    ScenarioType scenario;
    ZoneId anchorZone;
    LocalDateTime startTime;
    WeatherCondition weather;
    TrafficLevel trafficLevel;
    double passengerDemandMultiplier;
    double cancellationRiskMultiplier;
    double driverSpeedMultiplier;
    double airportFareBias;
    Instant startedAt;
}
