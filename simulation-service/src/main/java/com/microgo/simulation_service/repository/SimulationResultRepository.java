package com.microgo.simulation_service.repository;

import com.microgo.simulation_service.entity.SimulationResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SimulationResultRepository extends JpaRepository<SimulationResultEntity, UUID> {
}
