package com.microgo.optimization_service.domain;

import com.microgo.optimization_service.enums.ZoneId;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class DistanceMatrixFact {
    Map<ZoneId, Map<ZoneId, Integer>> travelMinutes;
    Map<ZoneId, Map<ZoneId, Double>> distanceKilometers;

    public int travelMinutes(ZoneId fromZone, ZoneId toZone) {
        return travelMinutes.getOrDefault(fromZone, Map.of()).getOrDefault(toZone, 20);
    }

    public double distanceKilometers(ZoneId fromZone, ZoneId toZone) {
        return distanceKilometers.getOrDefault(fromZone, Map.of()).getOrDefault(toZone, 8.0);
    }
}
