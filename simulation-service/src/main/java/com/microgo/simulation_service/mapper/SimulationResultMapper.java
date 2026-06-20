package com.microgo.simulation_service.mapper;

import com.microgo.simulation_service.domain.OptimizationSnapshot;
import com.microgo.simulation_service.entity.SimulationResultEntity;
import com.microgo.simulation_service.entity.SimulationRunEntity;

import java.util.UUID;

public final class SimulationResultMapper {

    private SimulationResultMapper() {
        // Private constructor to prevent instantiation
    }

    public static SimulationResultEntity toSimulationResultEntity(
            SimulationRunEntity runEntity,
            OptimizationSnapshot snapshot,
            String predictedDemandByZone,
            String metricsSnapshot) {
        return SimulationResultEntity.builder()
                .id(UUID.randomUUID())
                .simulationRun(runEntity)
                .activeScenario(snapshot.getActiveScenario())
                .predictedDemandByZone(predictedDemandByZone)
                .pendingRideRequests(snapshot.getPendingRideRequests())
                .driverAcceptanceProbability(snapshot.getDriverAcceptanceProbability())
                .averageWaitingTimeSeconds(snapshot.getAverageWaitingTimeSeconds())
                .cancellationRisk(snapshot.getCancellationRisk())
                .metricsSnapshot(metricsSnapshot)
                .completedAt(snapshot.getCompletedAt())
                .build();
    }
}
