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

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "baseline_vs_optimized_metrics")
public class BaselineVsOptimizedMetricsEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "optimization_run_id", nullable = false)
    private OptimizationRunEntity optimizationRun;

    @Column(name = "metric_name", nullable = false)
    private String metricName;

    @Column(name = "baseline_value", nullable = false)
    private double baselineValue;

    @Column(name = "optimized_value", nullable = false)
    private double optimizedValue;

    @Column(name = "improvement", nullable = false)
    private double improvement;

    @Column(name = "unit", nullable = false)
    private String unit;
}
