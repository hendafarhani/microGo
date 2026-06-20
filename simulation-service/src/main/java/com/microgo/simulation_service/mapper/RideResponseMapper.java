package com.microgo.simulation_service.mapper;

import com.microgo.simulation_service.domain.DriverAgent;
import com.microgo.simulation_service.domain.DriverDecision;
import com.microgo.simulation_service.domain.DriverDecisionContext;
import com.microgo.simulation_service.domain.DriverLocationSnapshot;
import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.enums.ZoneId;
import com.microgo.simulation_service.entity.DriverProfileEntity;
import com.microgo.simulation_service.kafka.model.DriverAcceptedEvent;
import com.microgo.simulation_service.kafka.model.DriverNotifiedEvent;
import com.microgo.simulation_service.kafka.model.DriverRefusedEvent;

import java.time.Instant;
import java.util.UUID;

public final class RideResponseMapper {

    private RideResponseMapper() {
        // Private constructor to prevent instantiation
    }

    public static DriverLocationSnapshot toFallbackLocationSnapshot(
            DriverNotifiedEvent event,
            DriverProfileEntity driverProfile,
            Instant occurredAt) {
        return DriverLocationSnapshot.builder()
                .driverId(event.getDriverId())
                .zone(driverProfile.getHomeZone())
                .latitude(event.getPickupLatitude())
                .longitude(event.getPickupLongitude())
                .available(true)
                .occurredAt(occurredAt)
                .build();
    }

    public static DriverDecisionContext toDriverDecisionContext(
            DriverNotifiedEvent event,
            ScenarioContext scenarioContext,
            DriverAgent driver,
            DriverLocationSnapshot liveLocation,
            ZoneId pickupZone,
            ZoneId destinationZone) {
        return DriverDecisionContext.builder()
                .scenarioContext(scenarioContext)
                .driver(driver)
                .liveLocation(liveLocation)
                .pickupZone(pickupZone)
                .destinationZone(destinationZone)
                .pickupLatitude(event.getPickupLatitude())
                .pickupLongitude(event.getPickupLongitude())
                .expectedFare(event.getExpectedFare())
                .build();
    }

    public static DriverAcceptedEvent toDriverAcceptedEvent(
            DriverNotifiedEvent event,
            UUID simulationRunId,
            DriverDecision decision,
            Instant decidedAt) {
        return DriverAcceptedEvent.builder()
                .simulationRunId(simulationRunId)
                .rideId(event.getRideId())
                .driverId(event.getDriverId())
                .passengerId(event.getPassengerId())
                .acceptanceProbability(decision.getAcceptanceProbability())
                .decidedAt(decidedAt)
                .build();
    }

    public static DriverRefusedEvent toDriverRefusedEvent(
            DriverNotifiedEvent event,
            UUID simulationRunId,
            DriverDecision decision,
            Instant decidedAt) {
        return DriverRefusedEvent.builder()
                .simulationRunId(simulationRunId)
                .rideId(event.getRideId())
                .driverId(event.getDriverId())
                .passengerId(event.getPassengerId())
                .acceptanceProbability(decision.getAcceptanceProbability())
                .reason(decision.getReason())
                .decidedAt(decidedAt)
                .build();
    }

    public static DriverAgent toDriverAgent(DriverProfileEntity entity) {
        return DriverAgent.builder()
                .id(entity.getId())
                .simulationRunId(entity.getSimulationRun().getId())
                .driverId(entity.getExternalDriverId())
                .homeZone(entity.getHomeZone())
                .fatigueScore(entity.getFatigueScore())
                .airportPreference(entity.getAirportPreference())
                .destinationBias(entity.getDestinationBias())
                .reliabilityScore(entity.getReliabilityScore())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
