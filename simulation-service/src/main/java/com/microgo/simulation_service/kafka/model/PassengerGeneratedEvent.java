package com.microgo.simulation_service.kafka.model;

import com.microgo.simulation_service.enums.ZoneId;
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
public class PassengerGeneratedEvent {
    private String passengerId;
    private UUID simulationRunId;
    private ZoneId originZone;
    private ZoneId destinationZone;
    private double urgencyScore;
    private Instant createdAt;
}
