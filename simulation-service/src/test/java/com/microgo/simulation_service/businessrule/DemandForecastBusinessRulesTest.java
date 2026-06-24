package com.microgo.simulation_service.businessrule;

import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.domain.SimulationMetricsSnapshot;
import com.microgo.simulation_service.enums.ScenarioType;
import com.microgo.simulation_service.enums.TrafficLevel;
import com.microgo.simulation_service.enums.ZoneId;
import com.microgo.simulation_service.enums.WeatherCondition;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DemandForecastBusinessRulesTest {

    @Test
    void shouldShapeAirportRushDemandTowardCentralLondonAndHeathrow() {
        Map<ZoneId, Integer> demandByZone = DemandForecastBusinessRules.buildPredictedDemandByZone(
                scenario(ScenarioType.AIRPORT_RUSH, ZoneId.CENTRAL_LONDON),
                snapshot(6));

        assertEquals(3, demandByZone.get(ZoneId.CENTRAL_LONDON));
        assertEquals(6, demandByZone.get(ZoneId.HEATHROW_CORRIDOR));
    }

    @Test
    void shouldKeepMinimumDemandForConcertForecastWhenPendingRequestsAreZero() {
        Map<ZoneId, Integer> demandByZone = DemandForecastBusinessRules.buildPredictedDemandByZone(
                scenario(ScenarioType.CONCERT_RAIN, ZoneId.WEMBLEY_EVENT_ZONE),
                snapshot(0));

        assertEquals(1, demandByZone.get(ZoneId.WEMBLEY_EVENT_ZONE));
        assertEquals(1, demandByZone.get(ZoneId.GENERAL_LONDON));
    }

    private ScenarioContext scenario(ScenarioType scenarioType, ZoneId anchorZone) {
        return ScenarioContext.builder()
                .simulationRunId(UUID.randomUUID())
                .scenario(scenarioType)
                .anchorZone(anchorZone)
                .startTime(LocalDateTime.now())
                .weather(WeatherCondition.CLEAR)
                .trafficLevel(TrafficLevel.MEDIUM)
                .passengerDemandMultiplier(2.0)
                .cancellationRiskMultiplier(1.0)
                .driverSpeedMultiplier(1.0)
                .airportFareBias(1.0)
                .startedAt(Instant.now())
                .build();
    }

    private SimulationMetricsSnapshot snapshot(int pendingRideRequests) {
        return SimulationMetricsSnapshot.builder()
                .simulationRunId(UUID.randomUUID())
                .pendingRideRequests(pendingRideRequests)
                .acceptedRides(0)
                .refusedRides(0)
                .cancelledRides(0)
                .averageWaitingTimeSeconds(0.0)
                .cancellationRisk(0.0)
                .acceptanceProbability(0.0)
                .updatedAt(Instant.now())
                .build();
    }
}

