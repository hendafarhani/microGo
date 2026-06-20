package com.microgo.simulation_service.mapper;

import com.microgo.simulation_service.domain.DriverLocationSnapshot;
import com.microgo.simulation_service.kafka.model.DriverLocationUpdatedEvent;

public final class DriverLocationSnapshotMapper {

    private DriverLocationSnapshotMapper() {
        // Private constructor to prevent instantiation
    }

    public static DriverLocationSnapshot toDriverLocationSnapshot(DriverLocationUpdatedEvent event) {
        return DriverLocationSnapshot.builder()
                .driverId(event.getDriverId())
                .zone(event.getZone())
                .latitude(event.getLatitude())
                .longitude(event.getLongitude())
                .available(event.isAvailable())
                .occurredAt(event.getOccurredAt())
                .build();
    }
}
