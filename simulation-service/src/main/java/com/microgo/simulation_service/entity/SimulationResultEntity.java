package com.microgo.simulation_service.entity;

import com.microgo.simulation_service.enums.ScenarioType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "simulation_result")
public class SimulationResultEntity {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "simulation_run_id", nullable = false, unique = true)
    private SimulationRunEntity simulationRun;

    @Enumerated(EnumType.STRING)
    @Column(name = "active_scenario", nullable = false)
    private ScenarioType activeScenario;

    @Column(name = "predicted_demand_by_zone", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String predictedDemandByZone;

    @Column(name = "pending_ride_requests", nullable = false)
    private int pendingRideRequests;

    @Column(name = "driver_acceptance_probability", nullable = false)
    private double driverAcceptanceProbability;

    @Column(name = "average_waiting_time_seconds", nullable = false)
    private double averageWaitingTimeSeconds;

    @Column(name = "cancellation_risk", nullable = false)
    private double cancellationRisk;

    @Column(name = "metrics_snapshot", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String metricsSnapshot;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;
}
