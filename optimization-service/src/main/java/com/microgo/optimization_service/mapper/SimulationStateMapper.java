package com.microgo.optimization_service.mapper;

import com.microgo.optimization_service.enums.ZoneId;
import com.microgo.optimization_service.kafka.model.ScenarioStartedEvent;
import com.microgo.optimization_service.kafka.model.SimulationCompletedEvent;
import com.microgo.optimization_service.kafka.model.SimulationMetricsUpdatedEvent;
import com.microgo.optimization_service.service.SimulationOutputReader.SimulationState;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SimulationStateMapper {

    public SimulationState fromScenarioStarted(
            ScenarioStartedEvent event,
            Map<ZoneId, Integer> predictedDemandByZone) {
        return SimulationState.builder()
                .simulationRunId(event.getSimulationRunId())
                .scenario(event.getScenario())
                .pendingRideRequests(0)
                .driverAcceptanceProbability(0.6)
                .averageWaitingTimeSeconds(240.0)
                .cancellationRisk(0.15)
                .predictedDemandByZone(predictedDemandByZone)
                .updatedAt(event.getStartedAt())
                .build();
    }

    public SimulationState fromMetricsUpdated(
            SimulationMetricsUpdatedEvent event,
            Map<ZoneId, Integer> predictedDemandByZone) {
        return SimulationState.builder()
                .simulationRunId(event.getSimulationRunId())
                .scenario(event.getScenario())
                .pendingRideRequests(event.getPendingRideRequests())
                .driverAcceptanceProbability(event.getDriverAcceptanceProbability())
                .averageWaitingTimeSeconds(event.getAverageWaitingTimeSeconds())
                .cancellationRisk(event.getCancellationRisk())
                .predictedDemandByZone(predictedDemandByZone)
                .updatedAt(event.getUpdatedAt())
                .build();
    }

    public SimulationState fromSimulationCompleted(
            SimulationCompletedEvent event,
            Map<ZoneId, Integer> predictedDemandByZone) {
        return SimulationState.builder()
                .simulationRunId(event.getSimulationRunId())
                .scenario(event.getActiveScenario())
                .pendingRideRequests(event.getPendingRideRequests())
                .driverAcceptanceProbability(event.getDriverAcceptanceProbability())
                .averageWaitingTimeSeconds(event.getAverageWaitingTimeSeconds())
                .cancellationRisk(event.getCancellationRisk())
                .predictedDemandByZone(predictedDemandByZone)
                .updatedAt(event.getCompletedAt())
                .build();
    }
}
