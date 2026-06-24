package com.microgo.simulation_service.kafka.publisher;

import com.microgo.simulation_service.domain.OptimizationSnapshot;
import com.microgo.simulation_service.domain.SimulationMetricsSnapshot;
import com.microgo.simulation_service.kafka.model.*;

public interface RideRequestPublisher {

    void publishScenarioStarted(ScenarioStartedEvent event);
    void publishDriverGenerated(DriverGeneratedEvent event);
    void publishPassengerGenerated(PassengerGeneratedEvent event);
    void publishRideRequested(SimulatedRideRequestedEvent event);
    void publishDriverAccepted(DriverAcceptedEvent event);
    void publishDriverRefused(DriverRefusedEvent event);
    void publishSimulationMetrics(SimulationMetricsSnapshot snapshot, String scenarioName);
    void publishSimulationCompleted(OptimizationSnapshot snapshot, String simulationRunId);
}
