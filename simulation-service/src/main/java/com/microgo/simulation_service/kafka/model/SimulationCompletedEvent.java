package com.microgo.simulation_service.kafka.model;

import com.microgo.simulation_service.enums.ScenarioType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationCompletedEvent {
    private UUID simulationRunId;
    private ScenarioType activeScenario;
    private Map<String, Integer> predictedDemandByZone;
    private int pendingRideRequests;
    private double driverAcceptanceProbability;
    private double averageWaitingTimeSeconds;
    private double cancellationRisk;
    private Instant completedAt;
}
