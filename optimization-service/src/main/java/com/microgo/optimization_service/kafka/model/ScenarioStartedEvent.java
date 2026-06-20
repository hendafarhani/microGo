package com.microgo.optimization_service.kafka.model;

import com.microgo.optimization_service.enums.ScenarioType;
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
public class ScenarioStartedEvent {
    private UUID simulationRunId;
    private ScenarioType scenario;
    private Instant startedAt;
}
