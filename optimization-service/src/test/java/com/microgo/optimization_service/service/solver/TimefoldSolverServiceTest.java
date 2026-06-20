package com.microgo.optimization_service.service.solver;

import com.microgo.optimization_service.config.OptimizationServiceProperties;
import com.microgo.optimization_service.domain.DriverSnapshot;
import com.microgo.optimization_service.enums.DriverStatus;
import com.microgo.optimization_service.domain.OptimizationSnapshot;
import com.microgo.optimization_service.enums.ScenarioType;
import com.microgo.optimization_service.enums.ZoneId;
import com.microgo.optimization_service.mapper.DistanceMatrixMapper;
import com.microgo.optimization_service.mapper.OptimizationComparisonMapper;
import com.microgo.optimization_service.mapper.TimefoldPlanningMapper;
import com.microgo.optimization_service.service.TimefoldSolverService;
import com.microgo.optimization_service.service.impl.BaselineVsOptimizedComparatorImpl;
import com.microgo.optimization_service.service.impl.DistanceMatrixSnapshotReaderImpl;
import com.microgo.optimization_service.service.impl.RideDispatchConstraintProviderImpl;
import com.microgo.optimization_service.service.impl.TimefoldSolverServiceImpl;
import com.microgo.optimization_service.domain.RideAvailabilityOptimizationSolution;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TimefoldSolverServiceTest {

    @Test
    void solveShouldPreserveMinimumCentralSupplyAndMoveDriversTowardHotZone() {
        OptimizationServiceProperties properties = new OptimizationServiceProperties();
        properties.setMinimumCentralLondonSupply(1);
        TimefoldSolverService solverService = new TimefoldSolverServiceImpl(
                new RideDispatchConstraintProviderImpl(properties),
                new BaselineVsOptimizedComparatorImpl(new OptimizationComparisonMapper()),
                new TimefoldPlanningMapper());

        OptimizationSnapshot snapshot = OptimizationSnapshot.builder()
                .simulationRunId(UUID.randomUUID())
                .activeScenario(ScenarioType.CONCERT_RAIN)
                .driverSnapshots(List.of(
                        driver("d1", ZoneId.CENTRAL_LONDON, 0.05),
                        driver("d2", ZoneId.CENTRAL_LONDON, 0.10),
                        driver("d3", ZoneId.GENERAL_LONDON, 0.15)))
                .pendingRideRequests(List.of())
                .predictedDemandByZone(Map.of(
                        ZoneId.WEMBLEY_EVENT_ZONE, 7,
                        ZoneId.CENTRAL_LONDON, 1,
                        ZoneId.GENERAL_LONDON, 1,
                        ZoneId.HEATHROW_CORRIDOR, 0))
                .trafficMultiplier(0.78)
                .cancellationRisk(0.35)
                .driverAcceptanceProbability(0.68)
                .averageWaitingTimeSeconds(390)
                .distanceMatrix(new DistanceMatrixSnapshotReaderImpl(new DistanceMatrixMapper())
                        .buildCurrentDistanceMatrix(0.78))
                .snapshotGeneratedAt(Instant.now())
                .build();

        RideAvailabilityOptimizationSolution solution = solverService.solveSnapshot(snapshot);

        assertThat(solution.getDriverRepositioningPlans())
                .anyMatch(plan -> plan.isMoveRecommended() && plan.getTargetZone() == ZoneId.WEMBLEY_EVENT_ZONE);
        long centralDriversMoved = solution.getDriverRepositioningPlans().stream()
                .filter(plan -> plan.isMoveRecommended() && plan.getDriver().getCurrentZone() == ZoneId.CENTRAL_LONDON)
                .count();
        assertThat(centralDriversMoved).isLessThanOrEqualTo(1);
        assertThat(solution.getComparison().getOptimizedMetrics().getAverageWaitSeconds())
                .isLessThan(solution.getComparison().getBaselineMetrics().getAverageWaitSeconds());
    }

    private DriverSnapshot driver(String driverId, ZoneId zoneId, double fatigueScore) {
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
                .fatigueScore(fatigueScore)
                .acceptanceProbability(0.74)
                .build();
    }
}
