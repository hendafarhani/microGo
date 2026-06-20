package com.microgo.optimization_service.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulatedRideRequestedEvent {
    private String rideId;
    private UUID simulationRunId;
    private String passengerId;
    private double pickupLatitude;
    private double pickupLongitude;
    private double destinationLatitude;
    private double destinationLongitude;
    private Instant requestedAt;
}
