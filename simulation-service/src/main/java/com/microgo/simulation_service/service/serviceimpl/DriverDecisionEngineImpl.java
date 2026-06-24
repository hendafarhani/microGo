package com.microgo.simulation_service.service.serviceimpl;

import com.microgo.simulation_service.businessrule.DriverDecisionBusinessRules;
import com.microgo.simulation_service.domain.DriverDecision;
import com.microgo.simulation_service.domain.DriverDecisionContext;
import com.microgo.simulation_service.mapper.DriverDecisionMapper;
import com.microgo.simulation_service.service.DriverDecisionEngine;
import org.springframework.stereotype.Service;

@Service
public class DriverDecisionEngineImpl implements DriverDecisionEngine {

    @Override
    public DriverDecision evaluate(DriverDecisionContext context) {
        DriverDecisionBusinessRules.DriverDecisionRuleOutcome outcome = DriverDecisionBusinessRules.evaluate(context);

        return DriverDecisionMapper.toDriverDecision(
                outcome.accepted(),
                outcome.acceptanceProbability(),
                outcome.refusalReason());
    }
}
