package com.microgo.simulation_service.mapper;

import com.microgo.simulation_service.domain.OptimizationSnapshot;
import com.microgo.simulation_service.enums.ScenarioType;
import com.microgo.simulation_service.domain.SimulationMetricsSnapshot;
import com.microgo.simulation_service.kafka.model.SimulationCompletedEvent;
import com.microgo.simulation_service.kafka.model.SimulationMetricsUpdatedEvent;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class RideRequestPublisherEventMapper {

    private RideRequestPublisherEventMapper() {
        // Private constructor to prevent instantiation
    }

    public static SimulationMetricsUpdatedEvent toSimulationMetricsUpdatedEvent(
            SimulationMetricsSnapshot snapshot,
            String scenarioName) {
        return SimulationMetricsUpdatedEvent.builder()
                .simulationRunId(snapshot.getSimulationRunId())
                .scenario(ScenarioType.valueOf(scenarioName))
                .pendingRideRequests(snapshot.getPendingRideRequests())
                .driverAcceptanceProbability(snapshot.getAcceptanceProbability())
                .averageWaitingTimeSeconds(snapshot.getAverageWaitingTimeSeconds())
                .cancellationRisk(snapshot.getCancellationRisk())
                .updatedAt(snapshot.getUpdatedAt())
                .build();
    }

    public static SimulationCompletedEvent toSimulationCompletedEvent(
            OptimizationSnapshot snapshot,
            String simulationRunId) {
        return SimulationCompletedEvent.builder()
                .simulationRunId(UUID.fromString(simulationRunId))
                .activeScenario(snapshot.getActiveScenario())
                .predictedDemandByZone(toDemandByZoneEventPayload(snapshot))
                .pendingRideRequests(snapshot.getPendingRideRequests())
                .driverAcceptanceProbability(snapshot.getDriverAcceptanceProbability())
                .averageWaitingTimeSeconds(snapshot.getAverageWaitingTimeSeconds())
                .cancellationRisk(snapshot.getCancellationRisk())
                .completedAt(snapshot.getCompletedAt())
                .build();
    }

    private static Map<String, Integer> toDemandByZoneEventPayload(OptimizationSnapshot snapshot) {
        return snapshot.getPredictedDemandByZone().entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().name(), Map.Entry::getValue));
    }
}
