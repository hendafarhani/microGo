package com.microgo.simulation_service.service;

import com.microgo.simulation_service.domain.PassengerAgent;
import com.microgo.simulation_service.domain.ScenarioContext;

public interface PassengerDecisionEngine {

    double calculateCancellationRisk(ScenarioContext context, PassengerAgent passenger, double waitingTimeSeconds);
}
