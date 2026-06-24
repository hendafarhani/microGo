package com.microgo.simulation_service.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DriverDecision {
    boolean accepted;
    double acceptanceProbability;
    String reason;
}
