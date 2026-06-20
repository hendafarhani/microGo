package com.microgo.optimization_service.entity;

import com.microgo.optimization_service.enums.OptimizationTrigger;
import com.microgo.optimization_service.enums.ScenarioType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "optimization_run")
public class OptimizationRunEntity {

    @Id
    private UUID id;

    @Column(name = "simulation_run_id", nullable = false)
    private UUID simulationRunId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scenario", nullable = false)
    private ScenarioType scenario;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_source", nullable = false)
    private OptimizationTrigger triggerSource;

    @Column(name = "solver_status", nullable = false)
    private String solverStatus;

    @Column(name = "snapshot_generated_at", nullable = false)
    private Instant snapshotGeneratedAt;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "baseline_strategy", nullable = false)
    private String baselineStrategy;

    @Column(name = "optimization_strategy", nullable = false)
    private String optimizationStrategy;

    @Column(name = "score_summary")
    private String scoreSummary;
}
