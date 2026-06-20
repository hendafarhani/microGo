package com.microgo.simulation_service.repository;

import com.microgo.simulation_service.entity.DriverProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DriverProfileRepository extends JpaRepository<DriverProfileEntity, UUID> {
    Optional<DriverProfileEntity> findByExternalDriverId(String externalDriverId);
}
