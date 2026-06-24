package com.microgo.optimization_service.service.impl;

import com.microgo.optimization_service.domain.RideRequestSnapshot;
import com.microgo.optimization_service.kafka.model.RideAssignedEvent;
import com.microgo.optimization_service.kafka.model.RideCancelledEvent;
import com.microgo.optimization_service.kafka.model.SimulatedRideRequestedEvent;
import com.microgo.optimization_service.mapper.RideRequestSnapshotMapper;
import com.microgo.optimization_service.service.DistanceMatrixSnapshotReader;
import com.microgo.optimization_service.service.RideRequestSnapshotReader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RideRequestSnapshotReaderImpl implements RideRequestSnapshotReader {

    private final DistanceMatrixSnapshotReader distanceMatrixSnapshotReader;
    private final RideRequestSnapshotMapper rideRequestSnapshotMapper;
    private final Map<String, RideRequestSnapshot> pendingRideRequests = new ConcurrentHashMap<>();

    public RideRequestSnapshotReaderImpl(
            DistanceMatrixSnapshotReader distanceMatrixSnapshotReader,
            RideRequestSnapshotMapper rideRequestSnapshotMapper) {
        this.distanceMatrixSnapshotReader = distanceMatrixSnapshotReader;
        this.rideRequestSnapshotMapper = rideRequestSnapshotMapper;
    }

    @Override
    public void onSimulatedRideRequested(SimulatedRideRequestedEvent event) {
        pendingRideRequests.put(event.getRideId(), mapRequestedRide(event));
    }

    @Override
    public void onRideAssigned(RideAssignedEvent event) {
        pendingRideRequests.remove(event.getRideId());
    }

    @Override
    public void onRideCancelled(RideCancelledEvent event) {
        pendingRideRequests.remove(event.getRideId());
    }

    @Override
    public List<RideRequestSnapshot> findPendingRideRequests(UUID simulationRunId) {
        return pendingRideRequests.values().stream()
                .filter(request -> simulationRunId == null || simulationRunId.equals(request.getSimulationRunId()))
                .sorted((left, right) -> left.getRequestedAt().compareTo(right.getRequestedAt()))
                .toList();
    }

    private RideRequestSnapshot mapRequestedRide(SimulatedRideRequestedEvent event) {
        return rideRequestSnapshotMapper.fromRequestedEvent(
                event,
                distanceMatrixSnapshotReader.resolveZone(event.getPickupLatitude(), event.getPickupLongitude()),
                distanceMatrixSnapshotReader.resolveZone(
                        event.getDestinationLatitude(),
                        event.getDestinationLongitude()));
    }
}
