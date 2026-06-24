package com.microgo.optimization_service.service.impl;

import com.microgo.optimization_service.businessrule.RideDispatchConstraintBusinessRules;
import com.microgo.optimization_service.businessrule.ZoneDemandBusinessRules;
import com.microgo.optimization_service.config.OptimizationServiceProperties;
import com.microgo.optimization_service.domain.DriverSnapshot;
import com.microgo.optimization_service.domain.OptimizationSnapshot;
import com.microgo.optimization_service.enums.ZoneId;
import com.microgo.optimization_service.domain.DistanceMatrixFact;
import com.microgo.optimization_service.service.RideDispatchConstraintProvider;
import org.springframework.stereotype.Component;

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
        return RideDispatchConstraintBusinessRules.eligibleDrivers(snapshot);
    }

    @Override
    public boolean isReachable(DriverSnapshot driverSnapshot, ZoneId targetZone, DistanceMatrixFact distanceMatrixFact) {
        return RideDispatchConstraintBusinessRules.isReachable(
                driverSnapshot,
                targetZone,
                distanceMatrixFact,
                properties.getReachabilityThresholdMinutes());
    }

    @Override
    public boolean wouldOvercrowd(ZoneId targetZone, Map<ZoneId, Integer> supplyByZone, Map<ZoneId, Integer> demandByZone) {
        return RideDispatchConstraintBusinessRules.wouldOvercrowd(
                targetZone,
                supplyByZone,
                demandByZone,
                properties.getMaxDriversPerTargetZone());
    }

    @Override
    public boolean wouldBreakCentralSupply(DriverSnapshot driverSnapshot, Map<ZoneId, Integer> supplyByZone) {
        return RideDispatchConstraintBusinessRules.wouldBreakCentralSupply(
                driverSnapshot,
                supplyByZone,
                properties.getMinimumCentralLondonSupply());
    }

    @Override
    public double priorityScore(DriverSnapshot driverSnapshot, ZoneId targetZone, Map<ZoneId, Integer> demandByZone,
                                DistanceMatrixFact distanceMatrixFact) {
        return RideDispatchConstraintBusinessRules.priorityScore(
                driverSnapshot,
                targetZone,
                demandByZone,
                distanceMatrixFact);
    }

    @Override
    public Map<ZoneId, Integer> demandByZone(OptimizationSnapshot snapshot) {
        return ZoneDemandBusinessRules.buildDemandByZone(snapshot);
    }
}
