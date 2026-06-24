package com.microgo.optimization_service.service;

import com.microgo.optimization_service.domain.OptimizationSnapshot;
import com.microgo.optimization_service.enums.ScenarioType;

import java.util.UUID;

public interface OptimizationSnapshotBuilder {

    OptimizationSnapshot buildOptimizationSnapshot(UUID simulationRunId, ScenarioType scenarioOverride);
}
