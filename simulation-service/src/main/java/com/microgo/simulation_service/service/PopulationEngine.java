package com.microgo.simulation_service.service;

import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.entity.SimulationRunEntity;
import com.microgo.simulation_service.domain.PopulationSnapshot;

public interface PopulationEngine {

    PopulationSnapshot bootstrapPopulation(ScenarioContext context, SimulationRunEntity runEntity);
}
