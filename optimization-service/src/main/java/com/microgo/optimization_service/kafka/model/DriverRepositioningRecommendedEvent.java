package com.microgo.optimization_service.kafka.model;

import com.microgo.optimization_service.enums.ScenarioType;
import com.microgo.optimization_service.enums.ZoneId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverRepositioningRecommendedEvent {
    private UUID optimizationRunId;
    private String driverId;
    private ScenarioType scenario;
    private ZoneId fromZone;
    private ZoneId targetZone;
    private double priorityScore;
    private int expectedWaitTimeReductionSeconds;
    private double expectedCancellationReduction;
    private Instant recommendedAt;
}
