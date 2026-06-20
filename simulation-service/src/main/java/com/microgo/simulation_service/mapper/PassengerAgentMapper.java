package com.microgo.simulation_service.mapper;

import com.microgo.simulation_service.domain.PassengerAgent;
import com.microgo.simulation_service.enums.ZoneId;

import java.time.Instant;
import java.util.UUID;

public final class PassengerAgentMapper {

    private PassengerAgentMapper() {
        // Private constructor to prevent instantiation
    }

    public static PassengerAgent toPassengerAgent(
            UUID simulationRunId,
            String passengerId,
            ZoneId originZone,
            ZoneId destinationZone,
            double urgencyScore,
            double cancellationSensitivity,
            double originLatitude,
            double originLongitude,
            double destinationLatitude,
            double destinationLongitude,
            Instant createdAt) {
        return PassengerAgent.builder()
                .id(UUID.randomUUID())
                .simulationRunId(simulationRunId)
                .passengerId(passengerId)
                .originZone(originZone)
                .destinationZone(destinationZone)
                .urgencyScore(urgencyScore)
                .cancellationSensitivity(cancellationSensitivity)
                .originLatitude(originLatitude)
                .originLongitude(originLongitude)
                .destinationLatitude(destinationLatitude)
                .destinationLongitude(destinationLongitude)
                .createdAt(createdAt)
                .build();
    }
}
