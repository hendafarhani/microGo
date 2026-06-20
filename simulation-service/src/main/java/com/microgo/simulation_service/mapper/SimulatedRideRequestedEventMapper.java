package com.microgo.simulation_service.mapper;

import com.microgo.simulation_service.domain.PassengerAgent;
import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.kafka.model.SimulatedRideRequestedEvent;

import java.time.Instant;

public final class SimulatedRideRequestedEventMapper {

    private SimulatedRideRequestedEventMapper() {
        // Private constructor to prevent instantiation
    }

    public static SimulatedRideRequestedEvent toRideRequestedEvent(
            ScenarioContext context,
            PassengerAgent passenger,
            String rideId,
            Instant requestedAt) {
        return SimulatedRideRequestedEvent.builder()
                .rideId(rideId)
                .simulationRunId(context.getSimulationRunId())
                .passengerId(passenger.getPassengerId())
                .pickupLatitude(passenger.getOriginLatitude())
                .pickupLongitude(passenger.getOriginLongitude())
                .destinationLatitude(passenger.getDestinationLatitude())
                .destinationLongitude(passenger.getDestinationLongitude())
                .requestedAt(requestedAt)
                .build();
    }
}
