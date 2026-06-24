package com.microgo.simulation_service.service.serviceimpl;

import com.microgo.simulation_service.businessrule.PassengerCancellationBusinessRules;
import com.microgo.simulation_service.domain.PassengerAgent;
import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.service.PassengerDecisionEngine;
import org.springframework.stereotype.Service;

@Service
public class PassengerDecisionEngineImpl implements PassengerDecisionEngine {

    @Override
    public double calculateCancellationRisk(ScenarioContext context, PassengerAgent passenger, double waitingTimeSeconds) {
        return PassengerCancellationBusinessRules.calculateRisk(context, passenger, waitingTimeSeconds);
    }
}
