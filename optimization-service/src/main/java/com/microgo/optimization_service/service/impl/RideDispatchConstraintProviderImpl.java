package com.microgo.optimization_service.service.impl;

import com.microgo.optimization_service.config.OptimizationServiceProperties;
import com.microgo.optimization_service.domain.DriverSnapshot;
import com.microgo.optimization_service.domain.OptimizationSnapshot;
import com.microgo.optimization_service.enums.ZoneId;
import com.microgo.optimization_service.domain.DistanceMatrixFact;
import com.microgo.optimization_service.service.RideDispatchConstraintProvider;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class RideDispatchConstraintProviderImpl implements RideDispatchConstraintProvider {

    private final OptimizationServiceProperties properties;

    public RideDispatchConstraintProviderImpl(OptimizationServiceProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<DriverSnapshot> eligibleDrivers(OptimizationSnapshot snapshot) {
        return snapshot.getDriverSnapshots().stream()
                .filter(DriverSnapshot::isRepositionable)
                .sorted(Comparator.comparingDouble(DriverSnapshot::getAcceptanceProbability).reversed()
                        .thenComparingDouble(DriverSnapshot::getFatigueScore))
                .toList();
    }

    @Override
    public boolean isReachable(DriverSnapshot driverSnapshot, ZoneId targetZone, DistanceMatrixFact distanceMatrixFact) {
        return distanceMatrixFact.travelMinutes(driverSnapshot.getCurrentZone(), targetZone)
                <= properties.getReachabilityThresholdMinutes();
    }

    @Override
    public boolean wouldOvercrowd(ZoneId targetZone, Map<ZoneId, Integer> supplyByZone, Map<ZoneId, Integer> demandByZone) {
        int targetSupply = supplyByZone.getOrDefault(targetZone, 0);
        int demand = demandByZone.getOrDefault(targetZone, 0);
        return targetSupply >= Math.max(properties.getMaxDriversPerTargetZone(), demand + 1);
    }

    @Override
    public boolean wouldBreakCentralSupply(DriverSnapshot driverSnapshot, Map<ZoneId, Integer> supplyByZone) {
        return driverSnapshot.getCurrentZone() == ZoneId.CENTRAL_LONDON
                && supplyByZone.getOrDefault(ZoneId.CENTRAL_LONDON, 0) <= properties.getMinimumCentralLondonSupply();
    }

    @Override
    public double priorityScore(DriverSnapshot driverSnapshot, ZoneId targetZone, Map<ZoneId, Integer> demandByZone,
                                DistanceMatrixFact distanceMatrixFact) {
        int demand = demandByZone.getOrDefault(targetZone, 0);
        int travelMinutes = distanceMatrixFact.travelMinutes(driverSnapshot.getCurrentZone(), targetZone);
        return demand * 1.0 + (driverSnapshot.getAcceptanceProbability() * 10.0)
                - (driverSnapshot.getFatigueScore() * 5.0) - (travelMinutes * 0.25);
    }

    @Override
    public Map<ZoneId, Integer> demandByZone(OptimizationSnapshot snapshot) {
        Map<ZoneId, Integer> demandByZone = new EnumMap<>(ZoneId.class);
        for (ZoneId zoneId : ZoneId.values()) {
            demandByZone.put(zoneId, snapshot.getPredictedDemandByZone().getOrDefault(zoneId, 0));
        }
        snapshot.getPendingRideRequests().forEach(request ->
                demandByZone.merge(request.getPickupZone(), 1, Integer::sum));
        return demandByZone;
    }
}
