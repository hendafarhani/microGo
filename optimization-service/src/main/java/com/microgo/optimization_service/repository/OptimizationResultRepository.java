package com.microgo.optimization_service.repository;

import com.microgo.optimization_service.entity.OptimizationResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OptimizationResultRepository extends JpaRepository<OptimizationResultEntity, UUID> {
    Optional<OptimizationResultEntity> findByOptimizationRunId(UUID optimizationRunId);
}
