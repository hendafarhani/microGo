package com.microgo.simulation_service.businessrule;

import com.microgo.simulation_service.domain.DriverAgent;
import com.microgo.simulation_service.domain.DriverDecisionContext;
import com.microgo.simulation_service.domain.DriverLocationSnapshot;
import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.enums.ScenarioType;
import com.microgo.simulation_service.enums.TrafficLevel;
import com.microgo.simulation_service.enums.WeatherCondition;
import com.microgo.simulation_service.enums.ZoneId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriverDecisionBusinessRulesTest {

    @Test
    void shouldAcceptAirportRushTripWithStrongDriverPreferenceAndFare() {
        DriverDecisionBusinessRules.DriverDecisionRuleOutcome outcome = DriverDecisionBusinessRules.evaluate(
                DriverDecisionContext.builder()
                        .scenarioContext(scenario(ScenarioType.AIRPORT_RUSH, WeatherCondition.CLEAR))
                        .driver(driver(0.15, 0.95, ZoneId.HEATHROW_CORRIDOR))
                        .liveLocation(location(51.5074, -0.1278, ZoneId.CENTRAL_LONDON))
                        .pickupZone(ZoneId.CENTRAL_LONDON)
                        .destinationZone(ZoneId.HEATHROW_CORRIDOR)
                        .pickupLatitude(51.5079)
                        .pickupLongitude(-0.1180)
                        .expectedFare(52.0)
                        .build());

        assertTrue(outcome.accepted());
        assertTrue(outcome.acceptanceProbability() >= 0.5);
        assertEquals("low-expected-utility", outcome.refusalReason());
    }

    @Test
    void shouldRefuseFarConcertRainPickupWithDriverFatigue() {
        DriverDecisionBusinessRules.DriverDecisionRuleOutcome outcome = DriverDecisionBusinessRules.evaluate(
                DriverDecisionContext.builder()
                        .scenarioContext(scenario(ScenarioType.CONCERT_RAIN, WeatherCondition.RAIN))
                        .driver(driver(0.90, 0.10, ZoneId.GENERAL_LONDON))
                        .liveLocation(location(51.4700, -0.4543, ZoneId.HEATHROW_CORRIDOR))
                        .pickupZone(ZoneId.WEMBLEY_EVENT_ZONE)
                        .destinationZone(ZoneId.CENTRAL_LONDON)
                        .pickupLatitude(51.5560)
                        .pickupLongitude(-0.2796)
                        .expectedFare(18.0)
                        .build());

        assertFalse(outcome.accepted());
        assertTrue(outcome.acceptanceProbability() < 0.5);
        assertEquals("pickup-too-far", outcome.refusalReason());
    }

    private ScenarioContext scenario(ScenarioType type, WeatherCondition weather) {
        return ScenarioContext.builder()
                .simulationRunId(UUID.randomUUID())
                .scenario(type)
                .anchorZone(type == ScenarioType.AIRPORT_RUSH ? ZoneId.CENTRAL_LONDON : ZoneId.WEMBLEY_EVENT_ZONE)
                .startTime(LocalDateTime.now())
                .weather(weather)
                .trafficLevel(TrafficLevel.HIGH)
                .passengerDemandMultiplier(2.0)
                .cancellationRiskMultiplier(1.2)
                .driverSpeedMultiplier(0.8)
                .airportFareBias(1.2)
                .startedAt(Instant.now())
                .build();
    }

    private DriverAgent driver(double fatigue, double airportPreference, ZoneId destinationBias) {
        return DriverAgent.builder()
                .id(UUID.randomUUID())
                .simulationRunId(UUID.randomUUID())
                .driverId("driver-1")
                .homeZone(ZoneId.CENTRAL_LONDON)
                .fatigueScore(fatigue)
                .airportPreference(airportPreference)
                .destinationBias(destinationBias)
                .reliabilityScore(0.8)
                .createdAt(Instant.now())
                .build();
    }

    private DriverLocationSnapshot location(double lat, double lon, ZoneId zone) {
        return DriverLocationSnapshot.builder()
                .driverId("driver-1")
                .zone(zone)
                .latitude(lat)
                .longitude(lon)
                .available(true)
                .occurredAt(Instant.now())
                .build();
    }
}

