package com.microgo.simulation_service.domain;

import com.microgo.simulation_service.enums.ZoneId;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class DriverLocationSnapshot {
    String driverId;
    ZoneId zone;
    double latitude;
    double longitude;
    boolean available;
    Instant occurredAt;
}
