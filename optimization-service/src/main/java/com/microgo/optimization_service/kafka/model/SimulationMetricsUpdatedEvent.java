package com.microgo.optimization_service.kafka.model;

import com.microgo.optimization_service.enums.ScenarioType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationMetricsUpdatedEvent {
    private UUID simulationRunId;
    private ScenarioType scenario;
    private int pendingRideRequests;
    private double driverAcceptanceProbability;
    private double averageWaitingTimeSeconds;
    private double cancellationRisk;
    private Instant updatedAt;
}
