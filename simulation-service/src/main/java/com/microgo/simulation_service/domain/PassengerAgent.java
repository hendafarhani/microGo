package com.microgo.simulation_service.domain;

import com.microgo.simulation_service.enums.ZoneId;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class PassengerAgent {
    UUID id;
    UUID simulationRunId;
    String passengerId;
    ZoneId originZone;
    ZoneId destinationZone;
    double urgencyScore;
    double cancellationSensitivity;
    double originLatitude;
    double originLongitude;
    double destinationLatitude;
    double destinationLongitude;
    Instant createdAt;
}
