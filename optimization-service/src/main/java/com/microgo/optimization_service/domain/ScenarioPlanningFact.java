package com.microgo.optimization_service.domain;

import com.microgo.optimization_service.enums.ScenarioType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ScenarioPlanningFact {
    ScenarioType scenarioType;
    double trafficMultiplier;
    double cancellationRisk;
    double acceptanceProbability;
}
