package com.microgo.simulation_service.service;

import com.microgo.simulation_service.domain.PassengerAgent;
import com.microgo.simulation_service.domain.ScenarioContext;

import java.util.List;

public interface PassengerPopulationGenerator {

    List<PassengerAgent> generatePassengers(ScenarioContext context);
}
