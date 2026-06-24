package com.microgo.optimization_service.kafka.publisher;

import com.microgo.optimization_service.kafka.model.DriverRepositioningRecommendedEvent;
import com.microgo.optimization_service.kafka.model.OptimizedAssignmentRecommendedEvent;
import com.microgo.optimization_service.kafka.model.OptimizationCompletedEvent;
import com.microgo.optimization_service.kafka.model.OptimizationRequestedEvent;

import java.util.List;

public interface RecommendationPublisher {

    void publishRequested(OptimizationRequestedEvent event);

    void publishDriverRecommendations(List<DriverRepositioningRecommendedEvent> events);

    void publishAssignmentRecommendations(List<OptimizedAssignmentRecommendedEvent> events);

    void publishCompleted(OptimizationCompletedEvent event);
}
