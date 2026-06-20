package com.microgo.optimization_service.domain;

import com.microgo.optimization_service.enums.ZoneId;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DriverRecommendation {
    String driverId;
    ZoneId currentZone;
    ZoneId targetZone;
    double distanceKm;
    double priorityScore;
    int expectedWaitReductionSeconds;
    double expectedCancellationReduction;
}
