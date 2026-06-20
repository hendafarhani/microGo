package com.microgo.simulation_service.mapper;

import com.microgo.simulation_service.domain.OptimizationSnapshot;
import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.domain.SimulationMetricsSnapshot;
import com.microgo.simulation_service.enums.ZoneId;

import java.time.Instant;
import java.util.Map;

public final class OptimizationSnapshotMapper {

    private OptimizationSnapshotMapper() {
        // Private constructor to prevent instantiation
    }

    public static OptimizationSnapshot toOptimizationSnapshot(
            ScenarioContext context,
            SimulationMetricsSnapshot snapshot,
            Map<ZoneId, Integer> predictedDemandByZone,
            Instant completedAt) {
        return OptimizationSnapshot.builder()
                .activeScenario(context.getScenario())
                .predictedDemandByZone(predictedDemandByZone)
                .pendingRideRequests(snapshot.getPendingRideRequests())
                .driverAcceptanceProbability(snapshot.getAcceptanceProbability())
                .averageWaitingTimeSeconds(snapshot.getAverageWaitingTimeSeconds())
                .cancellationRisk(snapshot.getCancellationRisk())
                .metricsSnapshot(snapshot)
                .completedAt(completedAt)
                .build();
    }
}
