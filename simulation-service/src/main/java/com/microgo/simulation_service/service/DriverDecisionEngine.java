package com.microgo.simulation_service.service;

import com.microgo.simulation_service.domain.DriverDecision;
import com.microgo.simulation_service.domain.DriverDecisionContext;

public interface DriverDecisionEngine {

    DriverDecision evaluate(DriverDecisionContext context);
}
