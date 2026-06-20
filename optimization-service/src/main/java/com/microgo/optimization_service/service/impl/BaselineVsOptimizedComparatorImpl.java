package com.microgo.optimization_service.service.impl;

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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BaselineVsOptimizedComparatorImpl implements BaselineVsOptimizedComparator {

    private static final int HIGH_DEMAND_ZONE_LIMIT = 2;

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
        Map<ZoneId, Integer> demandByZone = calculateDemandByZone(snapshot);
        Map<ZoneId, Integer> supplyByZone = calculateSupplyByZone(snapshot, targetZones);
        int unmetRequestCount = countUnmetRequests(demandByZone, supplyByZone);
        int totalDemand = calculateTotalDemand(demandByZone);
        double shortageRatio = unmetRequestCount / (double) totalDemand;
        int maximumZoneShortage = findMaximumZoneShortage(demandByZone, supplyByZone);
        double highDemandCoverageRatio = calculateHighDemandCoverageRatio(demandByZone, supplyByZone);
        double averageWaitSeconds = snapshot.getAverageWaitingTimeSeconds() * (0.8 + shortageRatio)
                + (maximumZoneShortage * 18.0);
        double p95WaitSeconds = averageWaitSeconds + (maximumZoneShortage * 45.0);
        double cancellationRisk = clamp(snapshot.getCancellationRisk() * (0.82 + shortageRatio)
                * (1.05 - (highDemandCoverageRatio * 0.25)));

        return optimizationComparisonMapper.toMetrics(
                averageWaitSeconds,
                p95WaitSeconds,
                cancellationRisk,
                highDemandCoverageRatio,
                unmetRequestCount,
                supplyByZone.getOrDefault(ZoneId.CENTRAL_LONDON, 0));
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

    private Map<ZoneId, Integer> calculateDemandByZone(OptimizationSnapshot snapshot) {
        Map<ZoneId, Integer> demandByZone = new EnumMap<>(ZoneId.class);
        for (ZoneId zoneId : ZoneId.values()) {
            demandByZone.put(zoneId, snapshot.getPredictedDemandByZone().getOrDefault(zoneId, 0));
        }
        snapshot.getPendingRideRequests().forEach(request ->
                demandByZone.merge(request.getPickupZone(), 1, Integer::sum));
        return demandByZone;
    }

    private Map<ZoneId, Integer> calculateSupplyByZone(
            OptimizationSnapshot snapshot,
            Map<String, ZoneId> targetZones) {
        Map<ZoneId, Integer> supplyByZone = new EnumMap<>(ZoneId.class);
        for (ZoneId zoneId : ZoneId.values()) {
            supplyByZone.put(zoneId, 0);
        }
        snapshot.getDriverSnapshots().stream()
                .filter(DriverSnapshot::isRepositionable)
                .map(driver -> targetZones.getOrDefault(driver.getDriverId(), driver.getCurrentZone()))
                .forEach(zoneId -> supplyByZone.merge(zoneId, 1, Integer::sum));
        return supplyByZone;
    }

    private int calculateTotalDemand(Map<ZoneId, Integer> demandByZone) {
        return Math.max(1, demandByZone.values().stream().mapToInt(Integer::intValue).sum());
    }

    private int countUnmetRequests(
            Map<ZoneId, Integer> demandByZone,
            Map<ZoneId, Integer> supplyByZone) {
        return demandByZone.entrySet().stream()
                .mapToInt(entry -> calculateZoneShortage(entry, supplyByZone))
                .sum();
    }

    private int findMaximumZoneShortage(
            Map<ZoneId, Integer> demandByZone,
            Map<ZoneId, Integer> supplyByZone) {
        return demandByZone.entrySet().stream()
                .mapToInt(entry -> calculateZoneShortage(entry, supplyByZone))
                .max()
                .orElse(0);
    }

    private int calculateZoneShortage(
            Map.Entry<ZoneId, Integer> demandEntry,
            Map<ZoneId, Integer> supplyByZone) {
        return Math.max(0, demandEntry.getValue() - supplyByZone.getOrDefault(demandEntry.getKey(), 0));
    }

    private double calculateHighDemandCoverageRatio(
            Map<ZoneId, Integer> demandByZone,
            Map<ZoneId, Integer> supplyByZone) {
        List<Map.Entry<ZoneId, Integer>> highDemandZones = demandByZone.entrySet().stream()
                .sorted(Map.Entry.<ZoneId, Integer>comparingByValue().reversed())
                .limit(HIGH_DEMAND_ZONE_LIMIT)
                .toList();
        int highDemandTotal = Math.max(1, highDemandZones.stream().mapToInt(Map.Entry::getValue).sum());
        int coveredDemand = highDemandZones.stream()
                .mapToInt(entry -> Math.min(
                        entry.getValue(),
                        supplyByZone.getOrDefault(entry.getKey(), 0)))
                .sum();
        return coveredDemand / (double) highDemandTotal;
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
