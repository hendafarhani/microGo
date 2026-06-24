package com.microgo.simulation_service.businessrule;

import com.microgo.simulation_service.domain.PassengerAgent;
import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.enums.ScenarioType;
import com.microgo.simulation_service.enums.TrafficLevel;
import com.microgo.simulation_service.enums.WeatherCondition;
import com.microgo.simulation_service.enums.ZoneId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PassengerCancellationBusinessRulesTest {

    @Test
    void shouldIncreaseCancellationRiskForRainAndCongestion() {
        double risk = PassengerCancellationBusinessRules.calculateRisk(
                scenario(ScenarioType.CONCERT_RAIN, WeatherCondition.RAIN, TrafficLevel.HIGH),
                passenger(0.8, 0.75),
                420);

        assertTrue(risk > 0.5);
    }

    @Test
    void shouldApplyHigherUrgencyPenaltyDuringAirportRush() {
        ScenarioContext airportRush = scenario(ScenarioType.AIRPORT_RUSH, WeatherCondition.CLEAR, TrafficLevel.MEDIUM);

        double highUrgencyRisk = PassengerCancellationBusinessRules.calculateRisk(
                airportRush,
                passenger(0.95, 0.40),
                180);
        double lowerUrgencyRisk = PassengerCancellationBusinessRules.calculateRisk(
                airportRush,
                passenger(0.60, 0.40),
                180);

        assertTrue(highUrgencyRisk > lowerUrgencyRisk);
    }

    private ScenarioContext scenario(ScenarioType type, WeatherCondition weather, TrafficLevel trafficLevel) {
        return ScenarioContext.builder()
                .simulationRunId(UUID.randomUUID())
                .scenario(type)
                .anchorZone(type == ScenarioType.AIRPORT_RUSH ? ZoneId.CENTRAL_LONDON : ZoneId.WEMBLEY_EVENT_ZONE)
                .startTime(LocalDateTime.now())
                .weather(weather)
                .trafficLevel(trafficLevel)
                .passengerDemandMultiplier(2.0)
                .cancellationRiskMultiplier(1.4)
                .driverSpeedMultiplier(0.7)
                .airportFareBias(1.0)
                .startedAt(Instant.now())
                .build();
    }

    private PassengerAgent passenger(double urgencyScore, double cancellationSensitivity) {
        return PassengerAgent.builder()
                .id(UUID.randomUUID())
                .simulationRunId(UUID.randomUUID())
                .passengerId("passenger-1")
                .originZone(ZoneId.WEMBLEY_EVENT_ZONE)
                .destinationZone(ZoneId.CENTRAL_LONDON)
                .urgencyScore(urgencyScore)
                .cancellationSensitivity(cancellationSensitivity)
                .originLatitude(51.5560)
                .originLongitude(-0.2796)
                .destinationLatitude(51.5074)
                .destinationLongitude(-0.1278)
                .createdAt(Instant.now())
                .build();
    }
}

