package com.microgo.simulation_service.service;

import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.entity.SimulationResultEntity;
import com.microgo.simulation_service.entity.SimulationRunEntity;

import java.util.Optional;
import java.util.UUID;

public interface ScenarioEngine {

    UUID startSimulation(String scenarioName, String requestedBy);

    SimulationResultEntity stopSimulation(UUID simulationRunId);

    SimulationRunEntity getSimulationRun(UUID simulationRunId);

    Optional<ScenarioContext> findActiveScenarioByDriverId(String driverId);
}
