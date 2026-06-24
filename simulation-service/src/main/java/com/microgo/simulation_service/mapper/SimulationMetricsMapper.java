package com.microgo.simulation_service.mapper;

import com.microgo.simulation_service.domain.SimulationMetricsSnapshot;
import com.microgo.simulation_service.entity.SimulationMetricsEntity;
import com.microgo.simulation_service.entity.SimulationRunEntity;

import java.time.Instant;
import java.util.UUID;

public final class SimulationMetricsMapper {

    private SimulationMetricsMapper() {
        // Private constructor to prevent instantiation
    }

    public static SimulationMetricsEntity mapToInitialMetricsEntity(
            SimulationRunEntity runEntity,
            int pendingRideRequests,
            double cancellationRisk) {
        return SimulationMetricsEntity.builder()
                .id(UUID.randomUUID())
                .simulationRun(runEntity)
                .pendingRideRequests(pendingRideRequests)
                .acceptedRides(0)
                .refusedRides(0)
                .cancelledRides(0)
                .averageWaitingTimeSeconds(0.0)
                .cancellationRisk(cancellationRisk)
                .acceptanceProbability(0.0)
                .updatedAt(Instant.now())
                .build();
    }

    public static void markDriverDecision(
            SimulationMetricsEntity metrics,
            boolean accepted,
            double latestAcceptanceProbability) {
        if (accepted) {
            metrics.setAcceptedRides(metrics.getAcceptedRides() + 1);
            metrics.setPendingRideRequests(Math.max(0, metrics.getPendingRideRequests() - 1));
        } else {
            metrics.setRefusedRides(metrics.getRefusedRides() + 1);
        }

        int totalDecisions = metrics.getAcceptedRides() + metrics.getRefusedRides();
        double previousDecisionTotal = metrics.getAcceptanceProbability() * Math.max(0, totalDecisions - 1);
        metrics.setAcceptanceProbability((previousDecisionTotal + latestAcceptanceProbability) / Math.max(1, totalDecisions));
        metrics.setUpdatedAt(Instant.now());
    }

    public static void markRideAssigned(
            SimulationMetricsEntity metrics,
            double newObservedWaitingTimeSeconds) {
        metrics.setAverageWaitingTimeSeconds(metrics.getAverageWaitingTimeSeconds() == 0.0
                ? newObservedWaitingTimeSeconds
                : (metrics.getAverageWaitingTimeSeconds() + newObservedWaitingTimeSeconds) / 2.0);
        metrics.setUpdatedAt(Instant.now());
    }

    public static void markRideCancelled(
            SimulationMetricsEntity metrics,
            double cancellationRisk) {
        metrics.setCancelledRides(metrics.getCancelledRides() + 1);
        metrics.setCancellationRisk(cancellationRisk);
        metrics.setUpdatedAt(Instant.now());
    }

    public static SimulationMetricsSnapshot toMetricsSnapshot(SimulationMetricsEntity metrics) {
        return SimulationMetricsSnapshot.builder()
                .simulationRunId(metrics.getSimulationRun().getId())
                .pendingRideRequests(metrics.getPendingRideRequests())
                .acceptedRides(metrics.getAcceptedRides())
                .refusedRides(metrics.getRefusedRides())
                .cancelledRides(metrics.getCancelledRides())
                .averageWaitingTimeSeconds(metrics.getAverageWaitingTimeSeconds())
                .cancellationRisk(metrics.getCancellationRisk())
                .acceptanceProbability(metrics.getAcceptanceProbability())
                .updatedAt(metrics.getUpdatedAt())
                .build();
    }
}
