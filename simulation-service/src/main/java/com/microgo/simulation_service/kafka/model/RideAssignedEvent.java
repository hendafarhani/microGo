package com.microgo.simulation_service.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideAssignedEvent {
    private String rideId;
    private String driverId;
    private String passengerId;
    private Instant occurredAt;
}
