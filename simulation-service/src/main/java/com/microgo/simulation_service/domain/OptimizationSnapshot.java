package com.microgo.simulation_service.domain;

import com.microgo.simulation_service.enums.ScenarioType;
import com.microgo.simulation_service.enums.ZoneId;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Map;

@Value
@Builder
public class OptimizationSnapshot {
    ScenarioType activeScenario;
    Map<ZoneId, Integer> predictedDemandByZone;
    int pendingRideRequests;
    double driverAcceptanceProbability;
    double averageWaitingTimeSeconds;
    double cancellationRisk;
    SimulationMetricsSnapshot metricsSnapshot;
    Instant completedAt;
}
