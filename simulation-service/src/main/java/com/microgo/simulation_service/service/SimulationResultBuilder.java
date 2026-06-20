package com.microgo.simulation_service.service;

import com.microgo.simulation_service.domain.OptimizationSnapshot;
import com.microgo.simulation_service.entity.SimulationResultEntity;
import com.microgo.simulation_service.entity.SimulationRunEntity;

public interface SimulationResultBuilder {

    SimulationResultEntity build(SimulationRunEntity runEntity, OptimizationSnapshot snapshot);
}
