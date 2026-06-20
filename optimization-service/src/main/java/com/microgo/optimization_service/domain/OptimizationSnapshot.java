package com.microgo.optimization_service.domain;

import com.microgo.optimization_service.enums.ScenarioType;
import com.microgo.optimization_service.enums.ZoneId;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Value
@Builder
public class OptimizationSnapshot {
    UUID simulationRunId;
    ScenarioType activeScenario;
    List<DriverSnapshot> driverSnapshots;
    List<RideRequestSnapshot> pendingRideRequests;
    Map<ZoneId, Integer> predictedDemandByZone;
    double trafficMultiplier;
    double cancellationRisk;
    double driverAcceptanceProbability;
    double averageWaitingTimeSeconds;
    DistanceMatrixFact distanceMatrix;
    Instant snapshotGeneratedAt;
}
