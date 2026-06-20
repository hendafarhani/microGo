package com.microgo.optimization_service.service;

import com.microgo.optimization_service.domain.DistanceMatrixFact;
import com.microgo.optimization_service.domain.DriverSnapshot;
import com.microgo.optimization_service.domain.OptimizationSnapshot;
import com.microgo.optimization_service.enums.ZoneId;

import java.util.List;
import java.util.Map;

public interface RideDispatchConstraintProvider {

    List<DriverSnapshot> eligibleDrivers(OptimizationSnapshot snapshot);

    boolean isReachable(
            DriverSnapshot driverSnapshot,
            ZoneId targetZone,
            DistanceMatrixFact distanceMatrixFact);

    boolean wouldOvercrowd(
            ZoneId targetZone,
            Map<ZoneId, Integer> supplyByZone,
            Map<ZoneId, Integer> demandByZone);

    boolean wouldBreakCentralSupply(
            DriverSnapshot driverSnapshot,
            Map<ZoneId, Integer> supplyByZone);

    double priorityScore(
            DriverSnapshot driverSnapshot,
            ZoneId targetZone,
            Map<ZoneId, Integer> demandByZone,
            DistanceMatrixFact distanceMatrixFact);

    Map<ZoneId, Integer> demandByZone(OptimizationSnapshot snapshot);
}
