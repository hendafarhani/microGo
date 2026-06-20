package com.microgo.optimization_service.domain;

import com.microgo.optimization_service.enums.DriverStatus;
import com.microgo.optimization_service.enums.ScenarioType;
import com.microgo.optimization_service.enums.ZoneId;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
public class DriverSnapshot {
    String driverId;
    String providerIdentifier;
    ScenarioType scenario;
    DriverStatus status;
    ZoneId currentZone;
    double latitude;
    double longitude;
    boolean available;
    long tickSequence;
    Instant updatedAt;
    double fatigueScore;
    double acceptanceProbability;

    public boolean isRepositionable() {
        return available && status != DriverStatus.OFFLINE && status != DriverStatus.ON_TRIP;
    }
}
