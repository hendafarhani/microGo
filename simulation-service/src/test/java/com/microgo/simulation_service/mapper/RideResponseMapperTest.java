package com.microgo.simulation_service.mapper;

import com.microgo.simulation_service.domain.DriverDecision;
import com.microgo.simulation_service.kafka.model.DriverAcceptedEvent;
import com.microgo.simulation_service.kafka.model.DriverNotifiedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RideResponseMapperTest {

    @Test
    void acceptedEventCarriesPickupCoordinatesForTheGenerator() {
        UUID runId = UUID.randomUUID();
        Instant decidedAt = Instant.parse("2026-01-01T00:00:00Z");
        DriverNotifiedEvent notified = DriverNotifiedEvent.builder()
                .rideId("ride-1")
                .driverId("driver-1")
                .passengerId("pax-1")
                .pickupLatitude(51.5560)
                .pickupLongitude(-0.2796)
                .destinationLatitude(51.5074)
                .destinationLongitude(-0.1278)
                .expectedFare(18.5)
                .occurredAt(decidedAt)
                .build();
        DriverDecision decision = DriverDecision.builder()
                .accepted(true)
                .acceptanceProbability(0.73)
                .build();

        DriverAcceptedEvent accepted = RideResponseMapper.toDriverAcceptedEvent(notified, runId, decision, decidedAt);

        assertThat(accepted.getRideId()).isEqualTo("ride-1");
        assertThat(accepted.getDriverId()).isEqualTo("driver-1");
        assertThat(accepted.getPickupLatitude()).isEqualTo(51.5560);
        assertThat(accepted.getPickupLongitude()).isEqualTo(-0.2796);
        assertThat(accepted.getExpectedFare()).isEqualTo(18.5);
        assertThat(accepted.getAcceptanceProbability()).isEqualTo(0.73);
    }
}
