package com.microgo.optimization_service.service;

import com.microgo.optimization_service.domain.RideRequestSnapshot;
import com.microgo.optimization_service.kafka.model.RideAssignedEvent;
import com.microgo.optimization_service.kafka.model.RideCancelledEvent;
import com.microgo.optimization_service.kafka.model.SimulatedRideRequestedEvent;

import java.util.List;
import java.util.UUID;

public interface RideRequestSnapshotReader {

    void onSimulatedRideRequested(SimulatedRideRequestedEvent event);

    void onRideAssigned(RideAssignedEvent event);

    void onRideCancelled(RideCancelledEvent event);

    List<RideRequestSnapshot> findPendingRideRequests(UUID simulationRunId);
}
