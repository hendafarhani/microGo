package com.microgo.optimization_service.mapper;

import com.microgo.optimization_service.domain.RideRequestSnapshot;
import com.microgo.optimization_service.enums.ZoneId;
import com.microgo.optimization_service.kafka.model.SimulatedRideRequestedEvent;
import org.springframework.stereotype.Component;

@Component
public class RideRequestSnapshotMapper {

    public RideRequestSnapshot fromRequestedEvent(
            SimulatedRideRequestedEvent event,
            ZoneId pickupZone,
            ZoneId destinationZone) {
        return RideRequestSnapshot.builder()
                .rideId(event.getRideId())
                .simulationRunId(event.getSimulationRunId())
                .passengerId(event.getPassengerId())
                .pickupZone(pickupZone)
                .destinationZone(destinationZone)
                .pickupLatitude(event.getPickupLatitude())
                .pickupLongitude(event.getPickupLongitude())
                .destinationLatitude(event.getDestinationLatitude())
                .destinationLongitude(event.getDestinationLongitude())
                .requestedAt(event.getRequestedAt())
                .build();
    }
}
