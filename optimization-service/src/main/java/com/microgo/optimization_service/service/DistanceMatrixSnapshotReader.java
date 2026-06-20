package com.microgo.optimization_service.service;

import com.microgo.optimization_service.domain.DistanceMatrixFact;
import com.microgo.optimization_service.enums.ZoneId;

public interface DistanceMatrixSnapshotReader {

    DistanceMatrixFact buildCurrentDistanceMatrix(double trafficMultiplier);

    ZoneId resolveZone(double latitude, double longitude);
}
