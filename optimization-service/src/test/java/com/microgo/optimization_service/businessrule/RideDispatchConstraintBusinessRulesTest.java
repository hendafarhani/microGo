package com.microgo.optimization_service.businessrule;

import com.microgo.optimization_service.domain.DistanceMatrixFact;
import com.microgo.optimization_service.domain.DriverSnapshot;
import com.microgo.optimization_service.domain.OptimizationSnapshot;
import com.microgo.optimization_service.enums.DriverStatus;
import com.microgo.optimization_service.enums.ScenarioType;
import com.microgo.optimization_service.enums.ZoneId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RideDispatchConstraintBusinessRulesTest {

    @Test
    void shouldSortEligibleDriversByAcceptanceThenFatigue() {
        OptimizationSnapshot snapshot = OptimizationSnapshot.builder()
                .simulationRunId(UUID.randomUUID())
                .activeScenario(ScenarioType.CONCERT_RAIN)
                .driverSnapshots(List.of(
                        driver("d1", ZoneId.CENTRAL_LONDON, 0.70, 0.20, true, DriverStatus.CRUISING),
                        driver("d2", ZoneId.CENTRAL_LONDON, 0.80, 0.30, true, DriverStatus.CRUISING),
                        driver("d3", ZoneId.CENTRAL_LONDON, 0.80, 0.10, true, DriverStatus.CRUISING),
                        driver("d4", ZoneId.CENTRAL_LONDON, 0.95, 0.05, false, DriverStatus.OFFLINE)))
                .pendingRideRequests(List.of())
                .predictedDemandByZone(Map.of())
                .trafficMultiplier(0.78)
                .cancellationRisk(0.3)
                .driverAcceptanceProbability(0.7)
                .averageWaitingTimeSeconds(300)
                .distanceMatrix(testDistanceMatrix())
                .snapshotGeneratedAt(Instant.now())
                .build();

        assertThat(RideDispatchConstraintBusinessRules.eligibleDrivers(snapshot))
                .extracting(DriverSnapshot::getDriverId)
                .containsExactly("d3", "d2", "d1");
    }

    @Test
    void shouldPenalizeLongTravelAndBlockOvercrowding() {
        DriverSnapshot driver = driver("d1", ZoneId.CENTRAL_LONDON, 0.80, 0.20, true, DriverStatus.CRUISING);
        DistanceMatrixFact distanceMatrix = testDistanceMatrix();

        double wembleyScore = RideDispatchConstraintBusinessRules.priorityScore(
                driver,
                ZoneId.WEMBLEY_EVENT_ZONE,
                Map.of(ZoneId.WEMBLEY_EVENT_ZONE, 6),
                distanceMatrix);
        double centralScore = RideDispatchConstraintBusinessRules.priorityScore(
                driver,
                ZoneId.CENTRAL_LONDON,
                Map.of(ZoneId.CENTRAL_LONDON, 6),
                distanceMatrix);

        assertThat(wembleyScore).isLessThan(centralScore);
        assertThat(RideDispatchConstraintBusinessRules.wouldOvercrowd(
                ZoneId.WEMBLEY_EVENT_ZONE,
                Map.of(ZoneId.WEMBLEY_EVENT_ZONE, 8),
                Map.of(ZoneId.WEMBLEY_EVENT_ZONE, 2),
                8)).isTrue();
    }

    private DriverSnapshot driver(
            String driverId,
            ZoneId zoneId,
            double acceptanceProbability,
            double fatigueScore,
            boolean available,
            DriverStatus status) {
        return DriverSnapshot.builder()
                .driverId(driverId)
                .providerIdentifier(driverId)
                .scenario(ScenarioType.CONCERT_RAIN)
                .status(status)
                .currentZone(zoneId)
                .latitude(51.50)
                .longitude(-0.12)
                .available(available)
                .tickSequence(5)
                .updatedAt(Instant.now())
                .fatigueScore(fatigueScore)
                .acceptanceProbability(acceptanceProbability)
                .build();
    }

    private DistanceMatrixFact testDistanceMatrix() {
        return DistanceMatrixFact.builder()
                .travelMinutes(Map.of(
                        ZoneId.CENTRAL_LONDON, Map.of(
                                ZoneId.CENTRAL_LONDON, 7,
                                ZoneId.WEMBLEY_EVENT_ZONE, 25)))
                .distanceKilometers(Map.of(
                        ZoneId.CENTRAL_LONDON, Map.of(
                                ZoneId.CENTRAL_LONDON, 2.0,
                                ZoneId.WEMBLEY_EVENT_ZONE, 14.0)))
                .build();
    }
}

