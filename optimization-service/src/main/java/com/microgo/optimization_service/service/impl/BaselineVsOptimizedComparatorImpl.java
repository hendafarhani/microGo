package com.microgo.optimization_service.service.impl;

import com.microgo.optimization_service.businessrule.OptimizationMetricsBusinessRules;
import com.microgo.optimization_service.businessrule.ZoneDemandBusinessRules;
import com.microgo.optimization_service.domain.DriverSnapshot;
import com.microgo.optimization_service.domain.OptimizationComparison;
import com.microgo.optimization_service.domain.OptimizationMetrics;
import com.microgo.optimization_service.domain.OptimizationSnapshot;
import com.microgo.optimization_service.enums.ZoneId;
import com.microgo.optimization_service.mapper.OptimizationComparisonMapper;
import com.microgo.optimization_service.service.BaselineVsOptimizedComparator;
import com.microgo.optimization_service.domain.DriverRepositioningPlan;
import com.microgo.optimization_service.domain.RideAvailabilityOptimizationSolution;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BaselineVsOptimizedComparatorImpl implements BaselineVsOptimizedComparator {

    private final OptimizationComparisonMapper optimizationComparisonMapper;

    public BaselineVsOptimizedComparatorImpl(OptimizationComparisonMapper optimizationComparisonMapper) {
        this.optimizationComparisonMapper = optimizationComparisonMapper;
    }

    @Override
    public OptimizationComparison compareBaselineWithOptimized(
            OptimizationSnapshot snapshot,
            RideAvailabilityOptimizationSolution solution) {
        OptimizationMetrics baselineMetrics = calculateMetrics(snapshot, Map.of());
        OptimizationMetrics optimizedMetrics = calculateMetrics(
                snapshot,
                extractRecommendedTargetZones(solution));
        return optimizationComparisonMapper.toComparison(baselineMetrics, optimizedMetrics);
    }

    @Override
    public OptimizationMetrics calculateMetrics(
            OptimizationSnapshot snapshot,
            Map<String, ZoneId> targetZones) {
        Map<ZoneId, Integer> demandByZone = ZoneDemandBusinessRules.buildDemandByZone(snapshot);
        Map<ZoneId, Integer> supplyByZone = ZoneDemandBusinessRules.buildSupplyByZone(snapshot, targetZones);
        return OptimizationMetricsBusinessRules.calculateMetrics(snapshot, demandByZone, supplyByZone);
    }

    @Override
    public List<String> findBaselineNearestDriverIds(
            OptimizationSnapshot snapshot,
            ZoneId pickupZone,
            int limit) {
        return snapshot.getDriverSnapshots().stream()
                .filter(DriverSnapshot::isRepositionable)
                .sorted(Comparator.comparingInt(driver ->
                        snapshot.getDistanceMatrix().travelMinutes(driver.getCurrentZone(), pickupZone)))
                .limit(limit)
                .map(DriverSnapshot::getDriverId)
                .toList();
    }

    private Map<String, ZoneId> extractRecommendedTargetZones(
            RideAvailabilityOptimizationSolution solution) {
        return solution.getDriverRepositioningPlans().stream()
                .filter(DriverRepositioningPlan::isMoveRecommended)
                .collect(Collectors.toMap(
                        plan -> plan.getDriver().getDriverId(),
                        DriverRepositioningPlan::getTargetZone));
    }
}
