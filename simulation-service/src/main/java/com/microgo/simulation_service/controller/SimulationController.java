package com.microgo.simulation_service.controller;

import com.microgo.simulation_service.entity.SimulationResultEntity;
import com.microgo.simulation_service.entity.SimulationRunEntity;
import com.microgo.simulation_service.service.ScenarioEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/simulations")
@RequiredArgsConstructor
public class SimulationController {

    private final ScenarioEngine scenarioEngine;

    @PostMapping("/{scenarioName}/start")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, UUID> startSimulation(
            @PathVariable String scenarioName,
            @RequestParam(defaultValue = "system") String requestedBy) {
        return Map.of("simulationRunId", scenarioEngine.startSimulation(scenarioName, requestedBy));
    }

    @PostMapping("/{simulationRunId}/stop")
    @ResponseStatus(HttpStatus.OK)
    public SimulationResultEntity stopSimulation(@PathVariable UUID simulationRunId) {
        return scenarioEngine.stopSimulation(simulationRunId);
    }

    @GetMapping("/{simulationRunId}")
    public SimulationRunEntity getSimulation(@PathVariable UUID simulationRunId) {
        return scenarioEngine.getSimulationRun(simulationRunId);
    }
}
