package com.microgo.simulation_service.service;

import com.microgo.simulation_service.domain.SimulationMetricsSnapshot;
import com.microgo.simulation_service.entity.SimulationRunEntity;

import java.util.UUID;

public interface SimulationMetricsCollector {

    SimulationMetricsSnapshot initializeRun(SimulationRunEntity runEntity, int pendingRideRequests, double cancellationRisk);

    SimulationMetricsSnapshot recordDriverDecision(UUID simulationRunId, boolean accepted, double acceptanceProbability);

    SimulationMetricsSnapshot recordRideAssignment(UUID simulationRunId);

    SimulationMetricsSnapshot recordRideCancellation(UUID simulationRunId, double cancellationRisk);

    SimulationMetricsSnapshot snapshot(UUID simulationRunId);
}
