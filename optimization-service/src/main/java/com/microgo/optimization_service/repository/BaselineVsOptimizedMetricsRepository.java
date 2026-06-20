package com.microgo.optimization_service.repository;

import com.microgo.optimization_service.entity.BaselineVsOptimizedMetricsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BaselineVsOptimizedMetricsRepository extends JpaRepository<BaselineVsOptimizedMetricsEntity, UUID> {
    List<BaselineVsOptimizedMetricsEntity> findByOptimizationRunId(UUID optimizationRunId);
}
