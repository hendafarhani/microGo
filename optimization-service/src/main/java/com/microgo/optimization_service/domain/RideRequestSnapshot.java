package com.microgo.optimization_service.domain;

import com.microgo.optimization_service.enums.ZoneId;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class RideRequestSnapshot {
    String rideId;
    UUID simulationRunId;
    String passengerId;
    ZoneId pickupZone;
    ZoneId destinationZone;
    double pickupLatitude;
    double pickupLongitude;
    double destinationLatitude;
    double destinationLongitude;
    Instant requestedAt;
}
