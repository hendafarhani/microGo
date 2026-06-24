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
@Table(name = "optimization_result")
public class OptimizationResultEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "optimization_run_id", nullable = false)
    private OptimizationRunEntity optimizationRun;

    @Column(name = "driver_count", nullable = false)
    private int driverCount;

    @Column(name = "pending_ride_count", nullable = false)
    private int pendingRideCount;

    @Column(name = "demand_summary", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String demandSummary;

    @Column(name = "baseline_metrics", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String baselineMetrics;

    @Column(name = "optimized_metrics", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String optimizedMetrics;

    @Column(name = "score_hard")
    private Integer scoreHard;

    @Column(name = "score_soft")
    private Integer scoreSoft;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
