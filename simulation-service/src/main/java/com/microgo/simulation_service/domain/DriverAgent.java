package com.microgo.simulation_service.domain;

import com.microgo.simulation_service.enums.ZoneId;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class DriverAgent {
    UUID id;
    UUID simulationRunId;
    String driverId;
    ZoneId homeZone;
    double fatigueScore;
    double airportPreference;
    ZoneId destinationBias;
    double reliabilityScore;
    Instant createdAt;
}
