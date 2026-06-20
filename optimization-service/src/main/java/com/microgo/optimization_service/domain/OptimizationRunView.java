package com.microgo.optimization_service.domain;

import com.microgo.optimization_service.enums.ScenarioType;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Value
@Builder
public class OptimizationRunView {
    UUID optimizationRunId;
    UUID simulationRunId;
    ScenarioType scenario;
    String solverStatus;
    OptimizationMetrics baselineMetrics;
    OptimizationMetrics optimizedMetrics;
    List<DriverRecommendation> recommendations;
    Instant startedAt;
    Instant completedAt;
}
