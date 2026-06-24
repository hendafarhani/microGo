package com.microgo.simulation_service.service.serviceimpl;

import com.microgo.simulation_service.domain.SimulationMetricsSnapshot;
import com.microgo.simulation_service.entity.SimulationMetricsEntity;
import com.microgo.simulation_service.entity.SimulationRunEntity;
import com.microgo.simulation_service.mapper.SimulationMetricsMapper;
import com.microgo.simulation_service.repository.SimulationMetricsRepository;
import com.microgo.simulation_service.service.SimulationMetricsCollector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SimulationMetricsCollectorImpl implements SimulationMetricsCollector {

    private final SimulationMetricsRepository simulationMetricsRepository;

    @Override
    @Transactional
    public SimulationMetricsSnapshot initializeRun(SimulationRunEntity runEntity, int pendingRideRequests, double cancellationRisk) {
        SimulationMetricsEntity entity = SimulationMetricsMapper.mapToInitialMetricsEntity(
                runEntity,
                pendingRideRequests,
                cancellationRisk);
        simulationMetricsRepository.save(entity);
        return SimulationMetricsMapper.toMetricsSnapshot(entity);
    }

    @Override
    @Transactional
    public SimulationMetricsSnapshot recordDriverDecision(UUID simulationRunId, boolean accepted, double acceptanceProbability) {
        SimulationMetricsEntity metrics = getMetricsForRun(simulationRunId);
        SimulationMetricsMapper.markDriverDecision(metrics, accepted, acceptanceProbability);
        simulationMetricsRepository.save(metrics);
        return SimulationMetricsMapper.toMetricsSnapshot(metrics);
    }

    @Override
    @Transactional
    public SimulationMetricsSnapshot recordRideAssignment(UUID simulationRunId) {
        SimulationMetricsEntity metrics = getMetricsForRun(simulationRunId);
        // Keep the waiting-time metric as a lightweight rolling average so we can update it incrementally
        // from ride lifecycle events without recomputing from a full event history.
        SimulationMetricsMapper.markRideAssigned(metrics, 180.0);
        simulationMetricsRepository.save(metrics);
        return SimulationMetricsMapper.toMetricsSnapshot(metrics);
    }

    @Override
    @Transactional
    public SimulationMetricsSnapshot recordRideCancellation(UUID simulationRunId, double cancellationRisk) {
        SimulationMetricsEntity metrics = getMetricsForRun(simulationRunId);
        SimulationMetricsMapper.markRideCancelled(metrics, cancellationRisk);
        simulationMetricsRepository.save(metrics);
        return SimulationMetricsMapper.toMetricsSnapshot(metrics);
    }

    @Override
    @Transactional(readOnly = true)
    public SimulationMetricsSnapshot snapshot(UUID simulationRunId) {
        return SimulationMetricsMapper.toMetricsSnapshot(getMetricsForRun(simulationRunId));
    }

    private SimulationMetricsEntity getMetricsForRun(UUID simulationRunId) {
        return simulationMetricsRepository.findBySimulationRunId(simulationRunId)
                .orElseThrow(() -> new IllegalArgumentException("Simulation metrics not found for run " + simulationRunId));
    }
}
