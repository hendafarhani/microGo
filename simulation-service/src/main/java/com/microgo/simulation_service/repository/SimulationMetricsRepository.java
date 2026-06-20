package com.microgo.simulation_service.repository;

import com.microgo.simulation_service.entity.SimulationMetricsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SimulationMetricsRepository extends JpaRepository<SimulationMetricsEntity, UUID> {
    Optional<SimulationMetricsEntity> findBySimulationRunId(UUID simulationRunId);
}
