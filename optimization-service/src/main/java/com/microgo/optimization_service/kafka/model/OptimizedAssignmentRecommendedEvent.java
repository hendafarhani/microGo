package com.microgo.optimization_service.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizedAssignmentRecommendedEvent {
    private UUID optimizationRunId;
    private String rideId;
    private String recommendedDriverId;
    private List<String> baselineCandidateDriverIds;
    private int expectedPickupEtaSeconds;
    private Instant recommendedAt;
}
