package com.microgo.simulation_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "simulation_metrics")
public class SimulationMetricsEntity {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "simulation_run_id", nullable = false, unique = true)
    private SimulationRunEntity simulationRun;

    @Column(name = "pending_ride_requests", nullable = false)
    private int pendingRideRequests;

    @Column(name = "accepted_rides", nullable = false)
    private int acceptedRides;

    @Column(name = "refused_rides", nullable = false)
    private int refusedRides;

    @Column(name = "cancelled_rides", nullable = false)
    private int cancelledRides;

    @Column(name = "average_waiting_time_seconds", nullable = false)
    private double averageWaitingTimeSeconds;

    @Column(name = "cancellation_risk", nullable = false)
    private double cancellationRisk;

    @Column(name = "acceptance_probability", nullable = false)
    private double acceptanceProbability;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
