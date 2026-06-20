package com.microgo.simulation_service.kafka.handler;

import com.microgo.simulation_service.domain.DriverLocationSnapshot;
import com.microgo.simulation_service.kafka.model.DriverLocationUpdatedEvent;
import com.microgo.simulation_service.mapper.DriverLocationSnapshotMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DriverLocationListener {

    private final Map<String, DriverLocationSnapshot> latestLocations = new ConcurrentHashMap<>();

    @KafkaListener(
            id = "${simulation-service.listeners.driver-location-updated.id}",
            topics = "${simulation-service.topics.driver-location-updated}",
            groupId = "${simulation-service.consumers.driver-location-updated.group-id}",
            containerFactory = "driverLocationUpdatedEventListenerFactory"
    )
    public void onDriverLocationUpdated(DriverLocationUpdatedEvent event) {
        latestLocations.put(event.getDriverId(), DriverLocationSnapshotMapper.toDriverLocationSnapshot(event));
        log.debug("Tracked live location for driver {}", event.getDriverId());
    }

    public Optional<DriverLocationSnapshot> findLatestLocation(String driverId) {
        return Optional.ofNullable(latestLocations.get(driverId));
    }
}
