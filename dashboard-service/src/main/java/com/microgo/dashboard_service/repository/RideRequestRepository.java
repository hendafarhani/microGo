package com.microgo.dashboard_service.repository;

import com.microgo.dashboard_service.entity.RideRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RideRequestRepository extends JpaRepository<RideRequestEntity, Long> {
}
