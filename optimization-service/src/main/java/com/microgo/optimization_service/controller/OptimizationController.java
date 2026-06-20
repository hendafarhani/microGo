package com.microgo.optimization_service.controller;

import com.microgo.optimization_service.domain.OptimizationRunView;
import com.microgo.optimization_service.enums.OptimizationTrigger;
import com.microgo.optimization_service.enums.ScenarioType;
import com.microgo.optimization_service.service.SolverJobManager;
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
@RequestMapping("/api/v1/optimizations")
@RequiredArgsConstructor
public class OptimizationController {

    private final SolverJobManager solverJobManager;

    @PostMapping("/run")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, UUID> runOptimization(
            @RequestParam(required = false) UUID simulationRunId,
            @RequestParam(required = false) ScenarioType scenario,
            @RequestParam(defaultValue = "MANUAL") OptimizationTrigger trigger) {
        return Map.of("optimizationRunId",
                solverJobManager.runOptimization(simulationRunId, scenario, trigger));
    }

    @GetMapping("/{optimizationRunId}")
    public OptimizationRunView getOptimization(@PathVariable UUID optimizationRunId) {
        return solverJobManager.getRun(optimizationRunId);
    }

    @GetMapping("/scenarios/{scenario}/latest")
    public OptimizationRunView getLatestOptimizationForScenario(@PathVariable ScenarioType scenario) {
        return solverJobManager.latestForScenario(scenario);
    }
}
