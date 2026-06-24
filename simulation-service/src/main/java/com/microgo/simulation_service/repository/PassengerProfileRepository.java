package com.microgo.simulation_service.repository;

import com.microgo.simulation_service.entity.PassengerProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PassengerProfileRepository extends JpaRepository<PassengerProfileEntity, UUID> {
}
