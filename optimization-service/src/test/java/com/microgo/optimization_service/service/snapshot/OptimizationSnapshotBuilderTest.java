package com.microgo.optimization_service.service.snapshot;

import com.microgo.optimization_service.config.OptimizationServiceProperties;
import com.microgo.optimization_service.domain.DriverSnapshot;
import com.microgo.optimization_service.enums.DriverStatus;
import com.microgo.optimization_service.domain.OptimizationSnapshot;
import com.microgo.optimization_service.domain.RideRequestSnapshot;
import com.microgo.optimization_service.enums.ScenarioType;
import com.microgo.optimization_service.enums.ZoneId;
import com.microgo.optimization_service.kafka.model.DriverLocationUpdatedEvent;
import com.microgo.optimization_service.kafka.model.RideAssignedEvent;
import com.microgo.optimization_service.kafka.model.RideCancelledEvent;
import com.microgo.optimization_service.kafka.model.SimulatedRideRequestedEvent;
import com.microgo.optimization_service.mapper.DistanceMatrixMapper;
import com.microgo.optimization_service.mapper.OptimizationSnapshotMapper;
import com.microgo.optimization_service.mapper.SimulationStateMapper;
import com.microgo.optimization_service.service.*;
import com.microgo.optimization_service.service.impl.DistanceMatrixSnapshotReaderImpl;
import com.microgo.optimization_service.service.impl.OptimizationSnapshotBuilderImpl;
import com.microgo.optimization_service.service.impl.SimulationOutputReaderImpl;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OptimizationSnapshotBuilderTest {

    @Test
    void buildShouldMergeSimulationDriversAndPendingRequests() {
        OptimizationServiceProperties properties = new OptimizationServiceProperties();
        SimulationOutputReader simulationOutputReader =
                new SimulationOutputReaderImpl(properties, new SimulationStateMapper());
        UUID simulationRunId = UUID.randomUUID();
        simulationOutputReader.onScenarioStarted(com.microgo.optimization_service.kafka.model.ScenarioStartedEvent.builder()
                .simulationRunId(simulationRunId)
                .scenario(ScenarioType.CONCERT_RAIN)
                .startedAt(Instant.now())
                .build());

        DriverSnapshot expectedDriver = DriverSnapshot.builder()
                .driverId("driver-1")
                .providerIdentifier("driver-1")
                .scenario(ScenarioType.CONCERT_RAIN)
                .status(DriverStatus.CRUISING)
                .currentZone(ZoneId.CENTRAL_LONDON)
                .latitude(51.50)
                .longitude(-0.12)
                .available(true)
                .tickSequence(12)
                .updatedAt(Instant.now())
                .fatigueScore(0.12)
                .acceptanceProbability(0.71)
                .build();
        RideRequestSnapshot expectedRequest = RideRequestSnapshot.builder()
                .rideId("ride-1")
                .simulationRunId(simulationRunId)
                .passengerId("passenger-1")
                .pickupZone(ZoneId.WEMBLEY_EVENT_ZONE)
                .destinationZone(ZoneId.CENTRAL_LONDON)
                .pickupLatitude(51.55)
                .pickupLongitude(-0.27)
                .destinationLatitude(51.50)
                .destinationLongitude(-0.12)
                .requestedAt(Instant.now())
                .build();
        DriverSnapshotReader driverSnapshotReader = new FixedDriverSnapshotReader(expectedDriver);
        RideRequestSnapshotReader rideRequestSnapshotReader = new FixedRideRequestSnapshotReader(expectedRequest);
        DistanceMatrixSnapshotReader distanceMatrixSnapshotReader =
                new DistanceMatrixSnapshotReaderImpl(new DistanceMatrixMapper());

        OptimizationSnapshotBuilder builder = new OptimizationSnapshotBuilderImpl(
                simulationOutputReader,
                driverSnapshotReader,
                rideRequestSnapshotReader,
                distanceMatrixSnapshotReader,
                new OptimizationSnapshotMapper());

        OptimizationSnapshot snapshot = builder.buildOptimizationSnapshot(simulationRunId, null);

        assertThat(snapshot.getSimulationRunId()).isEqualTo(simulationRunId);
        assertThat(snapshot.getActiveScenario()).isEqualTo(ScenarioType.CONCERT_RAIN);
        assertThat(snapshot.getDriverSnapshots()).hasSize(1);
        assertThat(snapshot.getPendingRideRequests()).hasSize(1);
        assertThat(snapshot.getPredictedDemandByZone())
                .containsEntry(ZoneId.WEMBLEY_EVENT_ZONE, 1);
    }

    private static class FixedDriverSnapshotReader implements DriverSnapshotReader {
        private final DriverSnapshot driverSnapshot;

        private FixedDriverSnapshotReader(DriverSnapshot driverSnapshot) {
            this.driverSnapshot = driverSnapshot;
        }

        @Override
        public void onDriverLocationUpdated(DriverLocationUpdatedEvent event) {
            throw new UnsupportedOperationException("Not needed by this test");
        }

        @Override
        public List<DriverSnapshot> findCurrentDrivers(ScenarioType activeScenario) {
            return List.of(driverSnapshot);
        }
    }

    private static class FixedRideRequestSnapshotReader implements RideRequestSnapshotReader {
        private final RideRequestSnapshot rideRequestSnapshot;

        private FixedRideRequestSnapshotReader(RideRequestSnapshot rideRequestSnapshot) {
            this.rideRequestSnapshot = rideRequestSnapshot;
        }

        @Override
        public void onSimulatedRideRequested(SimulatedRideRequestedEvent event) {
            throw new UnsupportedOperationException("Not needed by this test");
        }

        @Override
        public void onRideAssigned(RideAssignedEvent event) {
            throw new UnsupportedOperationException("Not needed by this test");
        }

        @Override
        public void onRideCancelled(RideCancelledEvent event) {
            throw new UnsupportedOperationException("Not needed by this test");
        }

        @Override
        public List<RideRequestSnapshot> findPendingRideRequests(UUID simulationRunId) {
            return List.of(rideRequestSnapshot);
        }
    }
}
