package com.microgo.optimization_service.repository;

import com.microgo.optimization_service.entity.DriverRepositioningRecommendationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DriverRepositioningRecommendationRepository extends JpaRepository<DriverRepositioningRecommendationEntity, UUID> {
    List<DriverRepositioningRecommendationEntity> findByOptimizationRunId(UUID optimizationRunId);
}
