package com.microgo.optimization_service.mapper;

import com.microgo.optimization_service.domain.DriverSnapshot;
import com.microgo.optimization_service.domain.OptimizationSnapshot;
import com.microgo.optimization_service.domain.RideRequestSnapshot;
import com.microgo.optimization_service.enums.ScenarioType;
import com.microgo.optimization_service.service.SimulationOutputReader.SimulationState;
import com.microgo.optimization_service.domain.DistanceMatrixFact;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class OptimizationSnapshotMapper {

    public OptimizationSnapshot toOptimizationSnapshot(
            SimulationState simulationState,
            ScenarioType activeScenario,
            List<DriverSnapshot> drivers,
            List<RideRequestSnapshot> pendingRides,
            double trafficMultiplier,
            DistanceMatrixFact distanceMatrix,
            Instant generatedAt) {
        return OptimizationSnapshot.builder()
                .simulationRunId(simulationState.getSimulationRunId())
                .activeScenario(activeScenario)
                .driverSnapshots(drivers)
                .pendingRideRequests(pendingRides)
                .predictedDemandByZone(simulationState.getPredictedDemandByZone())
                .trafficMultiplier(trafficMultiplier)
                .cancellationRisk(simulationState.getCancellationRisk())
                .driverAcceptanceProbability(simulationState.getDriverAcceptanceProbability())
                .averageWaitingTimeSeconds(simulationState.getAverageWaitingTimeSeconds())
                .distanceMatrix(distanceMatrix)
                .snapshotGeneratedAt(generatedAt)
                .build();
    }
}
