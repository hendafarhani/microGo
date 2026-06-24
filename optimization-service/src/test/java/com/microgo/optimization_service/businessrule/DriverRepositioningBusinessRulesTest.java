package com.microgo.optimization_service.businessrule;

import com.microgo.optimization_service.domain.DistanceMatrixFact;
import com.microgo.optimization_service.domain.DriverSnapshot;
import com.microgo.optimization_service.domain.OptimizationComparison;
import com.microgo.optimization_service.domain.OptimizationMetrics;
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

class DriverRepositioningBusinessRulesTest {

    @Test
    void shouldRewardShortageCoverageAndPenalizeStayingInSameZone() {
        double movingScore = DriverRepositioningBusinessRules.calculateTargetZoneScore(4.0, 3, false);
        double stayingScore = DriverRepositioningBusinessRules.calculateTargetZoneScore(4.0, 3, true);

        assertThat(movingScore).isGreaterThan(stayingScore);
    }

    @Test
    void shouldCalculateWaitAndCancellationReductionWithMinimumsAndCaps() {
        OptimizationSnapshot snapshot = OptimizationSnapshot.builder()
                .simulationRunId(UUID.randomUUID())
                .activeScenario(ScenarioType.CONCERT_RAIN)
                .driverSnapshots(List.of())
                .pendingRideRequests(List.of())
                .predictedDemandByZone(Map.of())
                .trafficMultiplier(0.78)
                .cancellationRisk(2.0)
                .driverAcceptanceProbability(0.62)
                .averageWaitingTimeSeconds(420)
                .distanceMatrix(testDistanceMatrix())
                .snapshotGeneratedAt(Instant.now())
                .build();
        DriverSnapshot driver = driver("d1", ZoneId.CENTRAL_LONDON);

        assertThat(DriverRepositioningBusinessRules.calculateExpectedWaitReductionSeconds(
                driver,
                ZoneId.GENERAL_LONDON,
                snapshot)).isEqualTo(10);
        assertThat(DriverRepositioningBusinessRules.calculateExpectedCancellationReduction(snapshot))
                .isEqualTo(0.25);
    }

    @Test
    void shouldConvertImprovementIntoComparisonScore() {
        OptimizationComparison comparison = OptimizationComparison.builder()
                .baselineMetrics(OptimizationMetrics.builder()
                        .averageWaitSeconds(500)
                        .cancellationRisk(0.40)
                        .build())
                .optimizedMetrics(OptimizationMetrics.builder()
                        .averageWaitSeconds(430)
                        .cancellationRisk(0.28)
                        .build())
                .build();

        assertThat(DriverRepositioningBusinessRules.calculateComparisonScore(comparison))
                .isEqualTo(82);
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

    private DistanceMatrixFact testDistanceMatrix() {
        return DistanceMatrixFact.builder()
                .travelMinutes(Map.of(
                        ZoneId.CENTRAL_LONDON, Map.of(ZoneId.GENERAL_LONDON, 2)))
                .distanceKilometers(Map.of(
                        ZoneId.CENTRAL_LONDON, Map.of(ZoneId.GENERAL_LONDON, 1.5)))
                .build();
    }
}

