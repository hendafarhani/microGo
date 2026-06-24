package com.microgo.optimization_service.service;

import com.microgo.optimization_service.domain.OptimizationRunView;
import com.microgo.optimization_service.enums.OptimizationTrigger;
import com.microgo.optimization_service.enums.ScenarioType;

import java.util.UUID;

public interface SolverJobManager {

    UUID runOptimization(UUID simulationRunId, ScenarioType scenarioType, OptimizationTrigger trigger);

    OptimizationRunView getRun(UUID optimizationRunId);

    OptimizationRunView latestForScenario(ScenarioType scenarioType);
}
