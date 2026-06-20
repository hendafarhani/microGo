package com.microgo.optimization_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "driver_repositioning_recommendation")
public class DriverRepositioningRecommendationEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "optimization_run_id", nullable = false)
    private OptimizationRunEntity optimizationRun;

    @Column(name = "driver_id", nullable = false)
    private String driverId;

    @Column(name = "current_zone", nullable = false)
    private String currentZone;

    @Column(name = "target_zone", nullable = false)
    private String targetZone;

    @Column(name = "distance_km", nullable = false)
    private double distanceKm;

    @Column(name = "priority_score", nullable = false)
    private double priorityScore;

    @Column(name = "expected_wait_reduction_seconds")
    private Integer expectedWaitReductionSeconds;

    @Column(name = "expected_cancellation_reduction")
    private Double expectedCancellationReduction;

    @Column(name = "recommendation_status", nullable = false)
    private String recommendationStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
