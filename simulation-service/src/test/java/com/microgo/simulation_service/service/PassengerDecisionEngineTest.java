package com.microgo.simulation_service.service;

import com.microgo.simulation_service.domain.PassengerAgent;
import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.enums.ScenarioType;
import com.microgo.simulation_service.enums.TrafficLevel;
import com.microgo.simulation_service.enums.WeatherCondition;
import com.microgo.simulation_service.enums.ZoneId;
import com.microgo.simulation_service.service.serviceimpl.PassengerDecisionEngineImpl;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PassengerDecisionEngineTest {

    private final PassengerDecisionEngine passengerDecisionEngine = new PassengerDecisionEngineImpl();

    @Test
    void shouldIncreaseCancellationRiskForRainAndCongestion() {
        ScenarioContext context = ScenarioContext.builder()
                .simulationRunId(UUID.randomUUID())
                .scenario(ScenarioType.CONCERT_RAIN)
                .anchorZone(ZoneId.WEMBLEY_EVENT_ZONE)
                .startTime(LocalDateTime.now())
                .weather(WeatherCondition.RAIN)
                .trafficLevel(TrafficLevel.HIGH)
                .passengerDemandMultiplier(2.0)
                .cancellationRiskMultiplier(1.4)
                .driverSpeedMultiplier(0.7)
                .airportFareBias(1.0)
                .startedAt(Instant.now())
                .build();
        PassengerAgent passenger = PassengerAgent.builder()
                .id(UUID.randomUUID())
                .simulationRunId(UUID.randomUUID())
                .passengerId("passenger-1")
                .originZone(ZoneId.WEMBLEY_EVENT_ZONE)
                .destinationZone(ZoneId.CENTRAL_LONDON)
                .urgencyScore(0.8)
                .cancellationSensitivity(0.75)
                .originLatitude(51.5560)
                .originLongitude(-0.2796)
                .destinationLatitude(51.5074)
                .destinationLongitude(-0.1278)
                .createdAt(Instant.now())
                .build();

        double risk = passengerDecisionEngine.calculateCancellationRisk(context, passenger, 420);

        assertTrue(risk > 0.5);
    }
}
