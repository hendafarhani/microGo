package com.microgo.simulation_service.repository;

import com.microgo.simulation_service.enums.ScenarioType;
import com.microgo.simulation_service.entity.ScenarioConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ScenarioConfigRepository extends JpaRepository<ScenarioConfigEntity, UUID> {
    Optional<ScenarioConfigEntity> findByScenarioName(ScenarioType scenarioName);
}
