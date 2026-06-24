package com.microgo.simulation_service.domain;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class SimulationMetricsSnapshot {
    UUID simulationRunId;
    int pendingRideRequests;
    int acceptedRides;
    int refusedRides;
    int cancelledRides;
    double averageWaitingTimeSeconds;
    double cancellationRisk;
    double acceptanceProbability;
    Instant updatedAt;
}
