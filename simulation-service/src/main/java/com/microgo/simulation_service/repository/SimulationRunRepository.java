package com.microgo.simulation_service.repository;

import com.microgo.simulation_service.entity.SimulationRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SimulationRunRepository extends JpaRepository<SimulationRunEntity, UUID> {
}
