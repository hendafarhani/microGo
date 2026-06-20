package com.microgo.optimization_service.mapper;

import com.microgo.optimization_service.enums.ZoneId;
import com.microgo.optimization_service.domain.DistanceMatrixFact;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DistanceMatrixMapper {

    public DistanceMatrixFact toDistanceMatrix(
            Map<ZoneId, Map<ZoneId, Integer>> travelMinutes,
            Map<ZoneId, Map<ZoneId, Double>> distanceKilometers) {
        return DistanceMatrixFact.builder()
                .travelMinutes(travelMinutes)
                .distanceKilometers(distanceKilometers)
                .build();
    }
}
