package com.microgo.optimization_service.domain;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class RideAvailabilityOptimizationSolution {
    List<DriverPlanningEntity> drivers;
    List<RideRequestPlanningEntity> rideRequests;
    List<ZonePlanningFact> zones;
    ScenarioPlanningFact scenario;
    DistanceMatrixFact distanceMatrix;
    List<DriverRepositioningPlan> driverRepositioningPlans;
    String solverStatus;
    int scoreHard;
    int scoreSoft;
    String scoreSummary;
    OptimizationComparison comparison;
}
