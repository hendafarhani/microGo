package com.microgo.simulation_service.kafka.model;

import com.microgo.simulation_service.enums.ScenarioType;
import com.microgo.simulation_service.enums.ZoneId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverLocationUpdatedEvent {
    private String driverId;
    private String providerIdentifier;
    private ScenarioType scenario;
    private String status;
    private ZoneId zone;
    private double latitude;
    private double longitude;
    private boolean available;
    private long tickSequence;
    private Instant occurredAt;
}
