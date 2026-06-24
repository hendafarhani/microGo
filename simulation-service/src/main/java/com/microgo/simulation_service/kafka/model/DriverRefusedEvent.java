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
public class DriverRefusedEvent {
    private UUID simulationRunId;
    private String rideId;
    private String driverId;
    private String passengerId;
    private double acceptanceProbability;
    private String reason;
    private Instant decidedAt;
}
