package com.microgo.optimization_service.businessrule;

import com.microgo.optimization_service.domain.DistanceMatrixFact;
import com.microgo.optimization_service.domain.DriverSnapshot;
import com.microgo.optimization_service.domain.OptimizationSnapshot;
import com.microgo.optimization_service.enums.ZoneId;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class RideDispatchConstraintBusinessRules {

    private static final double ACCEPTANCE_WEIGHT = 10.0;
    private static final double FATIGUE_WEIGHT = 5.0;
    private static final double TRAVEL_MINUTE_PENALTY = 0.25;

    private RideDispatchConstraintBusinessRules() {
    }

    public static List<DriverSnapshot> eligibleDrivers(OptimizationSnapshot snapshot) {
        return snapshot.getDriverSnapshots().stream()
                .filter(DriverSnapshot::isRepositionable)
                .sorted(Comparator.comparingDouble(DriverSnapshot::getAcceptanceProbability).reversed()
                        .thenComparingDouble(DriverSnapshot::getFatigueScore))
                .toList();
    }

    public static boolean isReachable(
            DriverSnapshot driverSnapshot,
            ZoneId targetZone,
            DistanceMatrixFact distanceMatrixFact,
            int reachabilityThresholdMinutes) {
        return distanceMatrixFact.travelMinutes(driverSnapshot.getCurrentZone(), targetZone)
                <= reachabilityThresholdMinutes;
    }

    public static boolean wouldOvercrowd(
            ZoneId targetZone,
            Map<ZoneId, Integer> supplyByZone,
            Map<ZoneId, Integer> demandByZone,
            int maxDriversPerTargetZone) {
        int targetSupply = supplyByZone.getOrDefault(targetZone, 0);
        int demand = demandByZone.getOrDefault(targetZone, 0);
        return targetSupply >= Math.max(maxDriversPerTargetZone, demand + 1);
    }

    public static boolean wouldBreakCentralSupply(
            DriverSnapshot driverSnapshot,
            Map<ZoneId, Integer> supplyByZone,
            int minimumCentralLondonSupply) {
        return driverSnapshot.getCurrentZone() == ZoneId.CENTRAL_LONDON
                && supplyByZone.getOrDefault(ZoneId.CENTRAL_LONDON, 0) <= minimumCentralLondonSupply;
    }

    public static double priorityScore(
            DriverSnapshot driverSnapshot,
            ZoneId targetZone,
            Map<ZoneId, Integer> demandByZone,
            DistanceMatrixFact distanceMatrixFact) {
        int demand = demandByZone.getOrDefault(targetZone, 0);
        int travelMinutes = distanceMatrixFact.travelMinutes(driverSnapshot.getCurrentZone(), targetZone);
        return demand
                + (driverSnapshot.getAcceptanceProbability() * ACCEPTANCE_WEIGHT)
                - (driverSnapshot.getFatigueScore() * FATIGUE_WEIGHT)
                - (travelMinutes * TRAVEL_MINUTE_PENALTY);
    }
}

