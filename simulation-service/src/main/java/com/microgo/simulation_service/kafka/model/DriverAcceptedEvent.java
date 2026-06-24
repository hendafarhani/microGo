package com.microgo.simulation_service.kafka.model;

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
public class DriverAcceptedEvent {
    private UUID simulationRunId;
    private String rideId;
    private String driverId;
    private String passengerId;
    private double pickupLatitude;
    private double pickupLongitude;
    private double expectedFare;
    private double acceptanceProbability;
    private Instant decidedAt;
}
