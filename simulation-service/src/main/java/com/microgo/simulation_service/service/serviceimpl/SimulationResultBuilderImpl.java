package com.microgo.simulation_service.service.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microgo.simulation_service.domain.OptimizationSnapshot;
import com.microgo.simulation_service.entity.SimulationResultEntity;
import com.microgo.simulation_service.entity.SimulationRunEntity;
import com.microgo.simulation_service.mapper.SimulationResultMapper;
import com.microgo.simulation_service.service.SimulationResultBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SimulationResultBuilderImpl implements SimulationResultBuilder {

    private final ObjectMapper objectMapper;

    @Override
    public SimulationResultEntity build(SimulationRunEntity runEntity, OptimizationSnapshot snapshot) {
        return SimulationResultMapper.toSimulationResultEntity(
                runEntity,
                snapshot,
                writeJson(snapshot.getPredictedDemandByZone()),
                writeJson(buildMetricsSnapshotPayload(snapshot)));
    }

    // Store a compact, denormalized metrics payload so optimization and debugging tools can inspect
    // the final run state without rejoining multiple simulation tables.
    private Map<String, Object> buildMetricsSnapshotPayload(OptimizationSnapshot snapshot) {
        return Map.of(
                "pendingRideRequests", snapshot.getMetricsSnapshot().getPendingRideRequests(),
                "acceptedRides", snapshot.getMetricsSnapshot().getAcceptedRides(),
                "refusedRides", snapshot.getMetricsSnapshot().getRefusedRides(),
                "cancelledRides", snapshot.getMetricsSnapshot().getCancelledRides(),
                "averageWaitingTimeSeconds", snapshot.getMetricsSnapshot().getAverageWaitingTimeSeconds(),
                "acceptanceProbability", snapshot.getMetricsSnapshot().getAcceptanceProbability(),
                "cancellationRisk", snapshot.getMetricsSnapshot().getCancellationRisk()
        );
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize simulation result payload", exception);
        }
    }
}
