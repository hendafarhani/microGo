package com.microgo.optimization_service.domain;

import com.microgo.optimization_service.enums.DriverStatus;
import com.microgo.optimization_service.enums.ZoneId;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DriverPlanningEntity {
    String driverId;
    ZoneId currentZone;
    DriverStatus status;
    boolean available;
    double fatigueScore;
    double acceptanceProbability;
}
