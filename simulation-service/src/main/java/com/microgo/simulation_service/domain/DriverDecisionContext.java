package com.microgo.simulation_service.domain;

import com.microgo.simulation_service.enums.ZoneId;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DriverDecisionContext {
    ScenarioContext scenarioContext;
    DriverAgent driver;
    DriverLocationSnapshot liveLocation;
    ZoneId pickupZone;
    ZoneId destinationZone;
    double pickupLatitude;
    double pickupLongitude;
    double expectedFare;
}
