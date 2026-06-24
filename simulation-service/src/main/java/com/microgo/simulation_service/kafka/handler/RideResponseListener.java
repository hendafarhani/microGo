package com.microgo.simulation_service.kafka.handler;

import com.microgo.simulation_service.domain.DriverDecision;
import com.microgo.simulation_service.domain.DriverLocationSnapshot;
import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.enums.ZoneId;
import com.microgo.simulation_service.entity.DriverProfileEntity;
import com.microgo.simulation_service.kafka.model.DriverNotifiedEvent;
import com.microgo.simulation_service.kafka.model.DriverReachedDestinationEvent;
import com.microgo.simulation_service.kafka.model.DriverReachedPickupEvent;
import com.microgo.simulation_service.kafka.model.RideAssignedEvent;
import com.microgo.simulation_service.kafka.model.RideCancelledEvent;
import com.microgo.simulation_service.kafka.publisher.impl.RideRequestPublisherImpl;
import com.microgo.simulation_service.mapper.RideResponseMapper;
import com.microgo.simulation_service.repository.DriverProfileRepository;
import com.microgo.simulation_service.service.DriverAvailabilityRegistry;
import com.microgo.simulation_service.service.DriverDecisionEngine;
import com.microgo.simulation_service.service.ScenarioEngine;
import com.microgo.simulation_service.service.SimulationMetricsCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RideResponseListener {

    private final DriverProfileRepository driverProfileRepository;
    private final DriverLocationListener driverLocationListener;
    private final DriverDecisionEngine driverDecisionEngine;
    private final SimulationMetricsCollector simulationMetricsCollector;
    private final RideRequestPublisherImpl rideRequestPublisherImpl;
    private final ScenarioEngine scenarioEngine;
    private final DriverAvailabilityRegistry driverAvailabilityRegistry;

    @KafkaListener(
            id = "${simulation-service.listeners.driver-notified.id}",
            topics = "${simulation-service.topics.driver-notified}",
            groupId = "${simulation-service.consumers.driver-notified.group-id}",
            containerFactory = "driverNotifiedEventListenerFactory"
    )
    public void onDriverNotified(DriverNotifiedEvent event) {
        Optional<ScenarioContext> activeScenario = scenarioEngine.findActiveScenarioByDriverId(event.getDriverId());
        if (activeScenario.isEmpty()) {
            log.debug("Ignoring driver notification for {} because no active scenario was found", event.getDriverId());
            return;
        }

        DriverProfileEntity driverProfile = getDriverProfile(event.getDriverId());
        DriverLocationSnapshot liveLocation = resolveDriverLocation(event, driverProfile);
        DriverDecision decision = evaluateDriverDecision(event, activeScenario.get(), driverProfile, liveLocation);

        UUID simulationRunId = driverProfile.getSimulationRun().getId();
        recordDriverDecision(simulationRunId, decision);
        publishDriverDecision(event, simulationRunId, decision);
    }

    @KafkaListener(
            id = "${simulation-service.listeners.ride-assigned.id}",
            topics = "${simulation-service.topics.ride-assigned}",
            groupId = "${simulation-service.consumers.ride-assigned.group-id}",
            containerFactory = "rideAssignedEventListenerFactory"
    )
    public void onRideAssigned(RideAssignedEvent event) {
        driverProfileRepository.findByExternalDriverId(event.getDriverId())
                .ifPresent(profile -> updateMetricsForAssignedRide(profile.getSimulationRun().getId()));
    }

    @KafkaListener(
            id = "${simulation-service.listeners.ride-cancelled.id}",
            topics = "${simulation-service.topics.ride-cancelled}",
            groupId = "${simulation-service.consumers.ride-cancelled.group-id}",
            containerFactory = "rideCancelledEventListenerFactory"
    )
    public void onRideCancelled(RideCancelledEvent event) {
        // A cancelled ride frees the driver back into the dispatch availability pool.
        driverAvailabilityRegistry.markAvailable(event.getDriverId());
        driverProfileRepository.findByExternalDriverId(event.getDriverId())
                .ifPresent(profile -> updateMetricsForCancelledRide(profile.getSimulationRun().getId()));
    }

    @KafkaListener(
            id = "${simulation-service.listeners.driver-reached-pickup.id}",
            topics = "${simulation-service.topics.driver-reached-pickup}",
            groupId = "${simulation-service.consumers.driver-reached-pickup.group-id}",
            containerFactory = "driverReachedPickupEventListenerFactory"
    )
    public void onDriverReachedPickup(DriverReachedPickupEvent event) {
        log.debug("Driver {} reached pickup for ride {}", event.getDriverId(), event.getRideId());
    }

    @KafkaListener(
            id = "${simulation-service.listeners.driver-reached-destination.id}",
            topics = "${simulation-service.topics.driver-reached-destination}",
            groupId = "${simulation-service.consumers.driver-reached-destination.group-id}",
            containerFactory = "driverReachedDestinationEventListenerFactory"
    )
    public void onDriverReachedDestination(DriverReachedDestinationEvent event) {
        // Trip complete: the driver is available for dispatch again.
        driverAvailabilityRegistry.markAvailable(event.getDriverId());
        log.debug("Driver {} reached destination for ride {}", event.getDriverId(), event.getRideId());
    }

    private DriverProfileEntity getDriverProfile(String driverId) {
        return driverProfileRepository.findByExternalDriverId(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver profile not found for " + driverId));
    }

    private DriverLocationSnapshot resolveDriverLocation(DriverNotifiedEvent event, DriverProfileEntity driverProfile) {
        return driverLocationListener.findLatestLocation(event.getDriverId())
                .orElseGet(() -> createFallbackLocationSnapshot(event, driverProfile));
    }

    private DriverLocationSnapshot createFallbackLocationSnapshot(
            DriverNotifiedEvent event,
            DriverProfileEntity driverProfile) {
        return RideResponseMapper.toFallbackLocationSnapshot(event, driverProfile, Instant.now());
    }

    private DriverDecision evaluateDriverDecision(
            DriverNotifiedEvent event,
            ScenarioContext scenarioContext,
            DriverProfileEntity driverProfile,
            DriverLocationSnapshot liveLocation) {
        return driverDecisionEngine.evaluate(RideResponseMapper.toDriverDecisionContext(
                event,
                scenarioContext,
                RideResponseMapper.toDriverAgent(driverProfile),
                liveLocation,
                determineZone(event.getPickupLatitude(), event.getPickupLongitude()),
                determineZone(event.getDestinationLatitude(), event.getDestinationLongitude())));
    }

    private void recordDriverDecision(UUID simulationRunId, DriverDecision decision) {
        simulationMetricsCollector.recordDriverDecision(
                simulationRunId,
                decision.isAccepted(),
                decision.getAcceptanceProbability());
    }

    private void publishDriverDecision(DriverNotifiedEvent event, UUID simulationRunId, DriverDecision decision) {
        if (decision.isAccepted()) {
            publishDriverAcceptedEvent(event, simulationRunId, decision);
            return;
        }
        publishDriverRefusedEvent(event, simulationRunId, decision);
    }

    private void publishDriverAcceptedEvent(DriverNotifiedEvent event, UUID simulationRunId, DriverDecision decision) {
        // The driver committed to this ride, so it leaves the dispatch availability pool.
        driverAvailabilityRegistry.markBusy(event.getDriverId());
        rideRequestPublisherImpl.publishDriverAccepted(
                RideResponseMapper.toDriverAcceptedEvent(event, simulationRunId, decision, Instant.now()));
    }

    private void publishDriverRefusedEvent(DriverNotifiedEvent event, UUID simulationRunId, DriverDecision decision) {
        rideRequestPublisherImpl.publishDriverRefused(
                RideResponseMapper.toDriverRefusedEvent(event, simulationRunId, decision, Instant.now()));
    }

    private void updateMetricsForAssignedRide(UUID simulationRunId) {
        simulationMetricsCollector.recordRideAssignment(simulationRunId);
    }

    private void updateMetricsForCancelledRide(UUID simulationRunId) {
        simulationMetricsCollector.recordRideCancellation(simulationRunId, 0.35);
    }

    private ZoneId determineZone(double latitude, double longitude) {
        if (isWembleyEventZoneCoordinate(latitude, longitude)) {
            return ZoneId.WEMBLEY_EVENT_ZONE;
        }
        if (isHeathrowCorridorLongitude(longitude)) {
            return ZoneId.HEATHROW_CORRIDOR;
        }
        if (isCentralLondonLatitude(latitude)) {
            return ZoneId.CENTRAL_LONDON;
        }
        return ZoneId.GENERAL_LONDON;
    }

    private static boolean isCentralLondonLatitude(double latitude) {
        return latitude > 51.49 && latitude < 51.53;
    }

    private static boolean isHeathrowCorridorLongitude(double longitude) {
        return longitude < -0.40;
    }

    private static boolean isWembleyEventZoneCoordinate(double latitude, double longitude) {
        return latitude > 51.54 && longitude < -0.25;
    }
}
