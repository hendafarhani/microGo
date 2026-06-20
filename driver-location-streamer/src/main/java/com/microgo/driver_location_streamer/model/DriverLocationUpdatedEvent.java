package com.microgo.driver_location_streamer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverLocationUpdatedEvent implements Serializable {
    private String driverId;
    private String driverIdentifier;
    private String providerIdentifier;
    private String driverDisplayId;
    private String scenario;
    private String status;
    private String zone;
    private double latitude;
    private double longitude;
    private boolean available;
    private long tickSequence;
    private Instant occurredAt;
}
