package com.microgo.optimization_service.kafka.consumer;

import com.microgo.optimization_service.config.OptimizationServiceProperties;
import com.microgo.optimization_service.enums.OptimizationTrigger;
import com.microgo.optimization_service.kafka.model.DriverLocationUpdatedEvent;
import com.microgo.optimization_service.kafka.model.RideAssignedEvent;
import com.microgo.optimization_service.kafka.model.RideCancelledEvent;
import com.microgo.optimization_service.kafka.model.ScenarioStartedEvent;
import com.microgo.optimization_service.kafka.model.SimulatedRideRequestedEvent;
import com.microgo.optimization_service.kafka.model.SimulationCompletedEvent;
import com.microgo.optimization_service.kafka.model.SimulationMetricsUpdatedEvent;
import com.microgo.optimization_service.service.DriverSnapshotReader;
import com.microgo.optimization_service.service.RideRequestSnapshotReader;
import com.microgo.optimization_service.service.SimulationOutputReader;
import com.microgo.optimization_service.service.SolverJobManager;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OptimizationEventConsumer {

    private final SimulationOutputReader simulationOutputReader;
    private final DriverSnapshotReader driverSnapshotReader;
    private final RideRequestSnapshotReader rideRequestSnapshotReader;
    private final SolverJobManager solverJobManager;
    private final OptimizationServiceProperties properties;

    @KafkaListener(
            id = "${optimization-service.listeners.scenario-started-id}",
            topics = "${optimization-service.topics.scenario-started}",
            groupId = "${optimization-service.consumers.scenario-started-group-id}",
            containerFactory = "scenarioStartedEventListenerFactory")
    public void onScenarioStarted(ScenarioStartedEvent event) {
        simulationOutputReader.onScenarioStarted(event);
    }

    @KafkaListener(
            id = "${optimization-service.listeners.simulated-ride-requested-id}",
            topics = "${optimization-service.topics.simulated-ride-requested}",
            groupId = "${optimization-service.consumers.simulated-ride-requested-group-id}",
            containerFactory = "simulatedRideRequestedEventListenerFactory")
    public void onSimulatedRideRequested(SimulatedRideRequestedEvent event) {
        rideRequestSnapshotReader.onSimulatedRideRequested(event);
    }

    @KafkaListener(
            id = "${optimization-service.listeners.simulation-metrics-updated-id}",
            topics = "${optimization-service.topics.simulation-metrics-updated}",
            groupId = "${optimization-service.consumers.simulation-metrics-updated-group-id}",
            containerFactory = "simulationMetricsUpdatedEventListenerFactory")
    public void onSimulationMetricsUpdated(SimulationMetricsUpdatedEvent event) {
        simulationOutputReader.onSimulationMetricsUpdated(event);
        solverJobManager.runOptimization(event.getSimulationRunId(), event.getScenario(),
                OptimizationTrigger.SIMULATION_METRICS_UPDATED);
    }

    @KafkaListener(
            id = "${optimization-service.listeners.simulation-completed-id}",
            topics = "${optimization-service.topics.simulation-completed}",
            groupId = "${optimization-service.consumers.simulation-completed-group-id}",
            containerFactory = "simulationCompletedEventListenerFactory")
    public void onSimulationCompleted(SimulationCompletedEvent event) {
        simulationOutputReader.onSimulationCompleted(event);
        solverJobManager.runOptimization(event.getSimulationRunId(), event.getActiveScenario(),
                OptimizationTrigger.SIMULATION_COMPLETED);
    }

    @KafkaListener(
            id = "${optimization-service.listeners.driver-location-updated-id}",
            topics = "${optimization-service.topics.driver-location-updated}",
            groupId = "${optimization-service.consumers.driver-location-updated-group-id}",
            containerFactory = "driverLocationUpdatedEventListenerFactory")
    public void onDriverLocationUpdated(DriverLocationUpdatedEvent event) {
        driverSnapshotReader.onDriverLocationUpdated(event);
    }

    @KafkaListener(
            id = "${optimization-service.listeners.ride-assigned-id}",
            topics = "${optimization-service.topics.ride-assigned}",
            groupId = "${optimization-service.consumers.ride-assigned-group-id}",
            containerFactory = "rideAssignedEventListenerFactory")
    public void onRideAssigned(RideAssignedEvent event) {
        rideRequestSnapshotReader.onRideAssigned(event);
    }

    @KafkaListener(
            id = "${optimization-service.listeners.ride-cancelled-id}",
            topics = "${optimization-service.topics.ride-cancelled}",
            groupId = "${optimization-service.consumers.ride-cancelled-group-id}",
            containerFactory = "rideCancelledEventListenerFactory")
    public void onRideCancelled(RideCancelledEvent event) {
        rideRequestSnapshotReader.onRideCancelled(event);
    }
}
