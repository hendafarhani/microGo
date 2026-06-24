package com.microgo.simulation_service.service;

import com.microgo.simulation_service.domain.OptimizationSnapshot;
import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.domain.SimulationMetricsSnapshot;

public interface ForecastCalculator {

    OptimizationSnapshot calculate(ScenarioContext context, SimulationMetricsSnapshot snapshot);
}
