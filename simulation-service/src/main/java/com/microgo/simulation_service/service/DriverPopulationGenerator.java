package com.microgo.simulation_service.service;

import com.microgo.simulation_service.domain.DriverAgent;
import com.microgo.simulation_service.domain.ScenarioContext;

import java.util.List;

public interface DriverPopulationGenerator {

    List<DriverAgent> generateDrivers(ScenarioContext context);
}
