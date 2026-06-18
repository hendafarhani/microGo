package com.microgo.dashboard_service.repository;

import com.microgo.dashboard_service.entity.EventOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventOutboxRepository extends JpaRepository<EventOutboxEntity, Long> {
}
