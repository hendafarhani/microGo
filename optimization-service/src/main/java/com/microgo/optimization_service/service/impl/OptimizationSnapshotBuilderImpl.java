package com.microgo.optimization_service.service.impl;

import com.microgo.optimization_service.domain.OptimizationSnapshot;
import com.microgo.optimization_service.enums.ScenarioType;
import com.microgo.optimization_service.mapper.OptimizationSnapshotMapper;
import com.microgo.optimization_service.service.DistanceMatrixSnapshotReader;
import com.microgo.optimization_service.service.DriverSnapshotReader;
import com.microgo.optimization_service.service.OptimizationSnapshotBuilder;
import com.microgo.optimization_service.service.RideRequestSnapshotReader;
import com.microgo.optimization_service.service.SimulationOutputReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OptimizationSnapshotBuilderImpl implements OptimizationSnapshotBuilder {

    private final SimulationOutputReader simulationOutputReader;
    private final DriverSnapshotReader driverSnapshotReader;
    private final RideRequestSnapshotReader rideRequestSnapshotReader;
    private final DistanceMatrixSnapshotReader distanceMatrixSnapshotReader;
    private final OptimizationSnapshotMapper optimizationSnapshotMapper;

    @Override
    public OptimizationSnapshot buildOptimizationSnapshot(UUID simulationRunId, ScenarioType scenarioOverride) {
        SimulationOutputReader.SimulationState simulationState = findRequiredSimulationState(simulationRunId);
        ScenarioType activeScenario = resolveActiveScenario(simulationState, scenarioOverride);
        double trafficMultiplier = simulationOutputReader.resolveTrafficMultiplier(activeScenario);

        return optimizationSnapshotMapper.toOptimizationSnapshot(
                simulationState,
                activeScenario,
                driverSnapshotReader.findCurrentDrivers(activeScenario),
                rideRequestSnapshotReader.findPendingRideRequests(simulationState.getSimulationRunId()),
                trafficMultiplier,
                distanceMatrixSnapshotReader.buildCurrentDistanceMatrix(trafficMultiplier),
                Instant.now());
    }

    private SimulationOutputReader.SimulationState findRequiredSimulationState(UUID simulationRunId) {
        return simulationOutputReader.findSimulationState(simulationRunId)
                .orElseThrow(() -> new IllegalStateException(
                        "No simulation state is available yet; wait for scenario.started or simulation metrics events"));
    }

    private ScenarioType resolveActiveScenario(
            SimulationOutputReader.SimulationState simulationState,
            ScenarioType scenarioOverride) {
        return scenarioOverride != null ? scenarioOverride : simulationState.getScenario();
    }
}
