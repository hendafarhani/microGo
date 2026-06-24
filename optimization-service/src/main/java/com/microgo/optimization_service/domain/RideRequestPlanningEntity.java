package com.microgo.optimization_service.domain;

import com.microgo.optimization_service.enums.ZoneId;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RideRequestPlanningEntity {
    String rideId;
    ZoneId pickupZone;
    ZoneId destinationZone;
}
