package com.microgo.simulation_service.mapper;

import com.microgo.simulation_service.domain.OptimizationSnapshot;
import com.microgo.simulation_service.enums.SimulationRunStatus;
import com.microgo.simulation_service.entity.SimulationRunEntity;

public final class SimulationRunMapper {

    private SimulationRunMapper() {
        // Private constructor to prevent instantiation
    }

    public static void markRunAsCompleted(
            SimulationRunEntity runEntity,
            OptimizationSnapshot optimizationSnapshot) {
        runEntity.setStatus(SimulationRunStatus.COMPLETED);
        runEntity.setCompletedAt(optimizationSnapshot.getCompletedAt());
    }
}
