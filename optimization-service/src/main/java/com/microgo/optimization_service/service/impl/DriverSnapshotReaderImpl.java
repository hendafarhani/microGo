package com.microgo.optimization_service.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microgo.optimization_service.config.OptimizationServiceProperties;
import com.microgo.optimization_service.domain.DriverSnapshot;
import com.microgo.optimization_service.enums.ScenarioType;
import com.microgo.optimization_service.kafka.model.DriverLocationUpdatedEvent;
import com.microgo.optimization_service.mapper.DriverSnapshotMapper;
import com.microgo.optimization_service.service.DriverSnapshotReader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class DriverSnapshotReaderImpl implements DriverSnapshotReader {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final OptimizationServiceProperties properties;
    private final DriverSnapshotMapper driverSnapshotMapper;

    private final Map<String, DriverSnapshot> latestDriverEvents = new ConcurrentHashMap<>();

    @Override
    public void onDriverLocationUpdated(DriverLocationUpdatedEvent event) {
        latestDriverEvents.put(event.getDriverId(), driverSnapshotMapper.fromLocationEvent(event));
    }

    @Override
    public List<DriverSnapshot> findCurrentDrivers(ScenarioType activeScenario) {
        return new ArrayList<>(mergeEventAndRedisSnapshots().values()).stream()
                .filter(snapshot -> belongsToScenario(snapshot, activeScenario))
                .filter(this::isFreshEnough)
                .sorted(Comparator.comparing(DriverSnapshot::getDriverId))
                .toList();
    }

    private Map<String, DriverSnapshot> mergeEventAndRedisSnapshots() {
        Map<String, DriverSnapshot> mergedSnapshots = new ConcurrentHashMap<>(latestDriverEvents);
        readRedisSnapshots().forEach(snapshot -> mergedSnapshots.put(snapshot.getDriverId(), snapshot));
        return mergedSnapshots;
    }

    private List<DriverSnapshot> readRedisSnapshots() {
        Set<String> keys = stringRedisTemplate.keys(properties.getRedis().getStateKeyPrefix() + "*");
        if (keys == null) {
            return List.of();
        }
        return keys.stream()
                .map(key -> stringRedisTemplate.opsForValue().get(key))
                .map(this::deserializeRedisSnapshot)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<DriverSnapshot> deserializeRedisSnapshot(String rawState) {
        if (rawState == null || rawState.isBlank()) {
            return Optional.empty();
        }
        try {
            JsonNode root = objectMapper.readTree(rawState);
            if (!root.path("driverId").isTextual()) {
                return Optional.empty();
            }
            double[] coordinates = resolveCoordinates(root);
            return Optional.of(driverSnapshotMapper.fromRedisState(root, coordinates[0], coordinates[1]));
        } catch (IOException exception) {
            return Optional.empty();
        }
    }

    private double[] resolveCoordinates(JsonNode root) {
        JsonNode currentPosition = root.path("currentPosition");
        double latitude = currentPosition.path("latitude").asDouble(0.0);
        double longitude = currentPosition.path("longitude").asDouble(0.0);
        if (latitude != 0.0 || longitude != 0.0) {
            return new double[]{latitude, longitude};
        }
        return findGeoCoordinates(root.path("driverId").asText());
    }

    private double[] findGeoCoordinates(String driverId) {
        List<Point> points = stringRedisTemplate.opsForGeo()
                .position(properties.getRedis().getGeoKey(), driverId);
        if (points == null || points.isEmpty() || points.getFirst() == null) {
            return new double[]{0.0, 0.0};
        }
        return new double[]{points.getFirst().getY(), points.getFirst().getX()};
    }

    private boolean belongsToScenario(DriverSnapshot snapshot, ScenarioType activeScenario) {
        return snapshot.getScenario() == null
                || activeScenario == null
                || snapshot.getScenario() == activeScenario;
    }

    private boolean isFreshEnough(DriverSnapshot snapshot) {
        return snapshot.getUpdatedAt() == null
                || snapshot.getUpdatedAt().isAfter(
                        Instant.now().minusSeconds(properties.getStaleDriverThresholdSeconds()));
    }
}
