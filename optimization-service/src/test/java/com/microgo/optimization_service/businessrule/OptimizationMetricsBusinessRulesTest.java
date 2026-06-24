package com.microgo.optimization_service.businessrule;

import com.microgo.optimization_service.domain.DistanceMatrixFact;
import com.microgo.optimization_service.domain.DriverSnapshot;
import com.microgo.optimization_service.domain.OptimizationMetrics;
import com.microgo.optimization_service.domain.OptimizationSnapshot;
import com.microgo.optimization_service.domain.RideRequestSnapshot;
import com.microgo.optimization_service.enums.DriverStatus;
import com.microgo.optimization_service.enums.ScenarioType;
import com.microgo.optimization_service.enums.ZoneId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OptimizationMetricsBusinessRulesTest {

    @Test
    void shouldLowerWaitAndCancellationWhenHighDemandZoneCoverageImproves() {
        OptimizationSnapshot snapshot = OptimizationSnapshot.builder()
                .simulationRunId(UUID.randomUUID())
                .activeScenario(ScenarioType.CONCERT_RAIN)
                .driverSnapshots(List.of(
                        driver("d1", ZoneId.CENTRAL_LONDON),
                        driver("d2", ZoneId.CENTRAL_LONDON),
                        driver("d3", ZoneId.GENERAL_LONDON)))
                .pendingRideRequests(List.of(
                        ride("r1", ZoneId.WEMBLEY_EVENT_ZONE),
                        ride("r2", ZoneId.WEMBLEY_EVENT_ZONE)))
                .predictedDemandByZone(Map.of(
                        ZoneId.WEMBLEY_EVENT_ZONE, 6,
                        ZoneId.CENTRAL_LONDON, 1,
                        ZoneId.GENERAL_LONDON, 1,
                        ZoneId.HEATHROW_CORRIDOR, 0))
                .trafficMultiplier(0.78)
                .cancellationRisk(0.4)
                .driverAcceptanceProbability(0.62)
                .averageWaitingTimeSeconds(420)
                .distanceMatrix(testDistanceMatrix())
                .snapshotGeneratedAt(Instant.now())
                .build();

        Map<ZoneId, Integer> demandByZone = ZoneDemandBusinessRules.buildDemandByZone(snapshot);
        OptimizationMetrics baselineMetrics = OptimizationMetricsBusinessRules.calculateMetrics(
                snapshot,
                demandByZone,
                ZoneDemandBusinessRules.buildSupplyByZone(snapshot, Map.of()));
        OptimizationMetrics optimizedMetrics = OptimizationMetricsBusinessRules.calculateMetrics(
                snapshot,
                demandByZone,
                ZoneDemandBusinessRules.buildSupplyByZone(snapshot, Map.of("d1", ZoneId.WEMBLEY_EVENT_ZONE)));

        assertThat(optimizedMetrics.getAverageWaitSeconds())
                .isLessThan(baselineMetrics.getAverageWaitSeconds());
        assertThat(optimizedMetrics.getCancellationRisk())
                .isLessThan(baselineMetrics.getCancellationRisk());
        assertThat(optimizedMetrics.getHighDemandCoverageRatio())
                .isGreaterThan(baselineMetrics.getHighDemandCoverageRatio());
        assertThat(optimizedMetrics.getCentralLondonSupplyCount()).isEqualTo(1);
    }

    private DriverSnapshot driver(String driverId, ZoneId zoneId) {
        return DriverSnapshot.builder()
                .driverId(driverId)
                .providerIdentifier(driverId)
                .scenario(ScenarioType.CONCERT_RAIN)
                .status(DriverStatus.CRUISING)
                .currentZone(zoneId)
                .latitude(51.50)
                .longitude(-0.12)
                .available(true)
                .tickSequence(5)
                .updatedAt(Instant.now())
                .fatigueScore(0.1)
                .acceptanceProbability(0.7)
                .build();
    }

    private RideRequestSnapshot ride(String rideId, ZoneId pickupZone) {
        return RideRequestSnapshot.builder()
                .rideId(rideId)
                .simulationRunId(UUID.randomUUID())
                .passengerId("p-" + rideId)
                .pickupZone(pickupZone)
                .destinationZone(ZoneId.CENTRAL_LONDON)
                .pickupLatitude(51.55)
                .pickupLongitude(-0.27)
                .destinationLatitude(51.50)
                .destinationLongitude(-0.12)
                .requestedAt(Instant.now())
                .build();
    }

    private DistanceMatrixFact testDistanceMatrix() {
        return DistanceMatrixFact.builder()
                .travelMinutes(Map.of(
                        ZoneId.CENTRAL_LONDON, Map.of(ZoneId.WEMBLEY_EVENT_ZONE, 25, ZoneId.CENTRAL_LONDON, 7),
                        ZoneId.GENERAL_LONDON, Map.of(ZoneId.WEMBLEY_EVENT_ZONE, 18, ZoneId.GENERAL_LONDON, 10),
                        ZoneId.WEMBLEY_EVENT_ZONE, Map.of(ZoneId.WEMBLEY_EVENT_ZONE, 8)))
                .distanceKilometers(Map.of(
                        ZoneId.CENTRAL_LONDON, Map.of(ZoneId.WEMBLEY_EVENT_ZONE, 14.0, ZoneId.CENTRAL_LONDON, 2.0),
                        ZoneId.GENERAL_LONDON, Map.of(ZoneId.WEMBLEY_EVENT_ZONE, 9.0, ZoneId.GENERAL_LONDON, 4.0),
                        ZoneId.WEMBLEY_EVENT_ZONE, Map.of(ZoneId.WEMBLEY_EVENT_ZONE, 2.0)))
                .build();
    }
}

