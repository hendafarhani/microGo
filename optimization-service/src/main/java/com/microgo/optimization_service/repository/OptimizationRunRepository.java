package com.microgo.optimization_service.repository;

import com.microgo.optimization_service.enums.ScenarioType;
import com.microgo.optimization_service.entity.OptimizationRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OptimizationRunRepository extends JpaRepository<OptimizationRunEntity, UUID> {
    Optional<OptimizationRunEntity> findTopByScenarioOrderByStartedAtDesc(ScenarioType scenario);
}
