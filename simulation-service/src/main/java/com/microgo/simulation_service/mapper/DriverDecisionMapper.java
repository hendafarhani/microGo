package com.microgo.simulation_service.mapper;

import com.microgo.simulation_service.domain.DriverDecision;

public final class DriverDecisionMapper {

    private DriverDecisionMapper() {
        // Private constructor to prevent instantiation
    }

    public static DriverDecision toDriverDecision(
            boolean accepted,
            double acceptanceProbability,
            String refusalReason) {
        return DriverDecision.builder()
                .accepted(accepted)
                .acceptanceProbability(acceptanceProbability)
                .reason(accepted ? "accepted" : refusalReason)
                .build();
    }
}
