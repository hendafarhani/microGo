package com.microgo.optimization_service.service;

import com.microgo.optimization_service.enums.ScenarioType;
import com.microgo.optimization_service.enums.ZoneId;
import com.microgo.optimization_service.kafka.model.ScenarioStartedEvent;
import com.microgo.optimization_service.kafka.model.SimulationCompletedEvent;
import com.microgo.optimization_service.kafka.model.SimulationMetricsUpdatedEvent;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface SimulationOutputReader {

    void onScenarioStarted(ScenarioStartedEvent event);

    void onSimulationMetricsUpdated(SimulationMetricsUpdatedEvent event);

    void onSimulationCompleted(SimulationCompletedEvent event);

    Optional<SimulationState> findSimulationState(UUID simulationRunId);

    double resolveTrafficMultiplier(ScenarioType scenarioType);

    @Value
    @Builder
    class SimulationState {
        UUID simulationRunId;
        ScenarioType scenario;
        int pendingRideRequests;
        double driverAcceptanceProbability;
        double averageWaitingTimeSeconds;
        double cancellationRisk;
        Map<ZoneId, Integer> predictedDemandByZone;
        Instant updatedAt;
    }
}
