package com.microgo.simulation_service.mapper;

import com.microgo.simulation_service.domain.DriverAgent;
import com.microgo.simulation_service.enums.ZoneId;

import java.time.Instant;
import java.util.UUID;

public final class DriverAgentMapper {

    private DriverAgentMapper() {
        // Private constructor to prevent instantiation
    }

    public static DriverAgent toDriverAgent(
            UUID simulationRunId,
            String driverId,
            ZoneId homeZone,
            double fatigueScore,
            double airportPreference,
            ZoneId destinationBias,
            double reliabilityScore,
            Instant createdAt) {
        return DriverAgent.builder()
                .id(UUID.randomUUID())
                .simulationRunId(simulationRunId)
                .driverId(driverId)
                .homeZone(homeZone)
                .fatigueScore(fatigueScore)
                .airportPreference(airportPreference)
                .destinationBias(destinationBias)
                .reliabilityScore(reliabilityScore)
                .createdAt(createdAt)
                .build();
    }
}
