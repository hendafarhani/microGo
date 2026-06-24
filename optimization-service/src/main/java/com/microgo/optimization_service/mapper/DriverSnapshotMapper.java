package com.microgo.optimization_service.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.microgo.optimization_service.domain.DriverSnapshot;
import com.microgo.optimization_service.enums.DriverStatus;
import com.microgo.optimization_service.enums.ScenarioType;
import com.microgo.optimization_service.enums.ZoneId;
import com.microgo.optimization_service.kafka.model.DriverLocationUpdatedEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DriverSnapshotMapper {

    public DriverSnapshot fromLocationEvent(DriverLocationUpdatedEvent event) {
        return DriverSnapshot.builder()
                .driverId(event.getDriverId())
                .providerIdentifier(event.getProviderIdentifier())
                .scenario(event.getScenario())
                .status(parseStatus(event.getStatus()))
                .currentZone(event.getZone())
                .latitude(event.getLatitude())
                .longitude(event.getLongitude())
                .available(event.isAvailable())
                .tickSequence(event.getTickSequence())
                .updatedAt(event.getOccurredAt())
                .fatigueScore(calculateFatigueScore(event.getTickSequence()))
                .acceptanceProbability(resolveAcceptanceProbability(event.getScenario(), event.isAvailable()))
                .build();
    }

    public DriverSnapshot fromRedisState(JsonNode root, double latitude, double longitude) {
        String driverId = root.path("driverId").asText();
        ScenarioType scenario = parseScenario(root.path("scenario").asText(null));
        boolean available = root.path("available").asBoolean(false);
        long tickSequence = root.path("tickSequence").asLong(0L);

        return DriverSnapshot.builder()
                .driverId(driverId)
                .providerIdentifier(root.path("driverDisplayId").asText(driverId))
                .scenario(scenario)
                .status(parseStatus(root.path("status").asText("OFFLINE")))
                .currentZone(parseZone(root.path("currentZone").asText("GENERAL_LONDON")))
                .latitude(latitude)
                .longitude(longitude)
                .available(available)
                .tickSequence(tickSequence)
                .updatedAt(parseUpdatedAt(root.path("updatedAt")))
                .fatigueScore(calculateFatigueScore(tickSequence))
                .acceptanceProbability(resolveAcceptanceProbability(scenario, available))
                .build();
    }

    private DriverStatus parseStatus(String rawStatus) {
        return DriverStatus.valueOf(rawStatus == null ? "OFFLINE" : rawStatus);
    }

    private ScenarioType parseScenario(String rawScenario) {
        return rawScenario == null || rawScenario.isBlank() ? null : ScenarioType.valueOf(rawScenario);
    }

    private ZoneId parseZone(String rawZone) {
        return ZoneId.valueOf(rawZone == null ? "GENERAL_LONDON" : rawZone);
    }

    private Instant parseUpdatedAt(JsonNode updatedAtNode) {
        return updatedAtNode.isTextual() ? Instant.parse(updatedAtNode.asText()) : Instant.now();
    }

    private double calculateFatigueScore(long tickSequence) {
        return Math.min(1.0, (tickSequence % 100) / 100.0);
    }

    private double resolveAcceptanceProbability(ScenarioType scenario, boolean available) {
        if (!available) {
            return 0.0;
        }
        if (scenario == ScenarioType.CONCERT_RAIN) {
            return 0.64;
        }
        if (scenario == ScenarioType.AIRPORT_RUSH) {
            return 0.70;
        }
        return 0.60;
    }
}
