package com.microgo.simulation_service.kafka.publisher.impl;

import com.microgo.simulation_service.config.SimulationServiceProperties;
import com.microgo.simulation_service.domain.OptimizationSnapshot;
import com.microgo.simulation_service.domain.SimulationMetricsSnapshot;
import com.microgo.simulation_service.kafka.model.DriverAcceptedEvent;
import com.microgo.simulation_service.kafka.model.DriverGeneratedEvent;
import com.microgo.simulation_service.kafka.model.DriverRefusedEvent;
import com.microgo.simulation_service.kafka.model.PassengerGeneratedEvent;
import com.microgo.simulation_service.kafka.model.ScenarioStartedEvent;
import com.microgo.simulation_service.kafka.model.SimulatedRideRequestedEvent;
import com.microgo.simulation_service.kafka.model.SimulationCompletedEvent;
import com.microgo.simulation_service.kafka.model.SimulationMetricsUpdatedEvent;
import com.microgo.simulation_service.kafka.publisher.RideRequestPublisher;
import com.microgo.simulation_service.mapper.RideRequestPublisherEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RideRequestPublisherImpl implements RideRequestPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SimulationServiceProperties properties;

    public void publishScenarioStarted(ScenarioStartedEvent event) {
        sendEvent(properties.getTopics().getScenarioStarted(), event.getSimulationRunId().toString(), event);
    }

    public void publishDriverGenerated(DriverGeneratedEvent event) {
        sendEvent(properties.getTopics().getDriverGenerated(), event.getDriverId(), event);
    }

    public void publishPassengerGenerated(PassengerGeneratedEvent event) {
        sendEvent(properties.getTopics().getPassengerGenerated(), event.getPassengerId(), event);
    }

    public void publishRideRequested(SimulatedRideRequestedEvent event) {
        sendEvent(properties.getTopics().getSimulatedRideRequested(), event.getRideId(), event);
    }

    public void publishDriverAccepted(DriverAcceptedEvent event) {
        sendEvent(properties.getTopics().getDriverAccepted(), event.getDriverId(), event);
    }

    public void publishDriverRefused(DriverRefusedEvent event) {
        sendEvent(properties.getTopics().getDriverRefused(), event.getDriverId(), event);
    }

    public void publishSimulationMetrics(SimulationMetricsSnapshot snapshot, String scenarioName) {
        SimulationMetricsUpdatedEvent metricsEvent = RideRequestPublisherEventMapper
                .toSimulationMetricsUpdatedEvent(snapshot, scenarioName);
        sendEvent(properties.getTopics().getSimulationMetricsUpdated(), snapshot.getSimulationRunId().toString(), metricsEvent);
    }

    public void publishSimulationCompleted(OptimizationSnapshot snapshot, String simulationRunId) {
        SimulationCompletedEvent completionEvent = RideRequestPublisherEventMapper
                .toSimulationCompletedEvent(snapshot, simulationRunId);
        sendEvent(properties.getTopics().getSimulationCompleted(), simulationRunId, completionEvent);
    }

    private void sendEvent(String topic, String key, Object payload) {
        kafkaTemplate.send(topic, key, payload);
    }
}
