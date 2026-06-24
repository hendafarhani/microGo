package com.microgo.optimization_service.kafka.publisher.impl;

import com.microgo.optimization_service.config.OptimizationServiceProperties;
import com.microgo.optimization_service.kafka.model.DriverRepositioningRecommendedEvent;
import com.microgo.optimization_service.kafka.model.OptimizedAssignmentRecommendedEvent;
import com.microgo.optimization_service.kafka.model.OptimizationCompletedEvent;
import com.microgo.optimization_service.kafka.model.OptimizationRequestedEvent;
import com.microgo.optimization_service.kafka.publisher.RecommendationPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RecommendationPublisherImpl implements RecommendationPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OptimizationServiceProperties properties;

    @Override
    public void publishRequested(OptimizationRequestedEvent event) {
        kafkaTemplate.send(properties.getTopics().getOptimizationRequested(), event.getOptimizationRunId().toString(), event);
    }

    @Override
    public void publishDriverRecommendations(List<DriverRepositioningRecommendedEvent> events) {
        for (DriverRepositioningRecommendedEvent event : events) {
            kafkaTemplate.send(properties.getTopics().getDriverRepositioningRecommended(), event.getDriverId(), event);
        }
    }

    @Override
    public void publishAssignmentRecommendations(List<OptimizedAssignmentRecommendedEvent> events) {
        for (OptimizedAssignmentRecommendedEvent event : events) {
            kafkaTemplate.send(properties.getTopics().getOptimizedAssignmentRecommended(), event.getRideId(), event);
        }
    }

    @Override
    public void publishCompleted(OptimizationCompletedEvent event) {
        kafkaTemplate.send(properties.getTopics().getOptimizationCompleted(), event.getOptimizationRunId().toString(), event);
    }
}
