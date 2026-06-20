package com.microgo.optimization_service.service.impl;

import com.microgo.optimization_service.domain.DriverSnapshot;
import com.microgo.optimization_service.domain.OptimizationComparison;
import com.microgo.optimization_service.domain.OptimizationSnapshot;
import com.microgo.optimization_service.enums.ZoneId;
import com.microgo.optimization_service.mapper.TimefoldPlanningMapper;
import com.microgo.optimization_service.service.BaselineVsOptimizedComparator;
import com.microgo.optimization_service.service.TimefoldSolverService;
import com.microgo.optimization_service.domain.DriverRepositioningPlan;
import com.microgo.optimization_service.domain.RideAvailabilityOptimizationSolution;
import com.microgo.optimization_service.service.RideDispatchConstraintProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TimefoldSolverServiceImpl implements TimefoldSolverService {

    private final RideDispatchConstraintProvider constraintProvider;
    private final BaselineVsOptimizedComparator comparator;
    private final TimefoldPlanningMapper timefoldPlanningMapper;

    public TimefoldSolverServiceImpl(RideDispatchConstraintProvider constraintProvider,
                                     BaselineVsOptimizedComparator comparator,
                                     TimefoldPlanningMapper timefoldPlanningMapper) {
        this.constraintProvider = constraintProvider;
        this.comparator = comparator;
        this.timefoldPlanningMapper = timefoldPlanningMapper;
    }

    @Override
    public RideAvailabilityOptimizationSolution solveSnapshot(OptimizationSnapshot snapshot) {
        Map<ZoneId, Integer> demandByZone = constraintProvider.demandByZone(snapshot);
        Map<ZoneId, Integer> initialSupplyByZone = calculateCurrentSupplyByZone(snapshot);
        List<DriverRepositioningPlan> plans = createRepositioningPlans(
                snapshot,
                demandByZone,
                initialSupplyByZone);

        RideAvailabilityOptimizationSolution provisionalSolution =
                timefoldPlanningMapper.toProvisionalSolution(
                        snapshot,
                        demandByZone,
                        initialSupplyByZone,
                        plans);
        OptimizationComparison comparison =
                comparator.compareBaselineWithOptimized(snapshot, provisionalSolution);

        return timefoldPlanningMapper.attachComparison(
                provisionalSolution,
                comparison,
                calculateComparisonScore(comparison));
    }

    private List<DriverRepositioningPlan> createRepositioningPlans(
            OptimizationSnapshot snapshot,
            Map<ZoneId, Integer> demandByZone,
            Map<ZoneId, Integer> initialSupplyByZone) {
        Map<ZoneId, Integer> mutableSupplyByZone = new EnumMap<>(initialSupplyByZone);
        List<DriverRepositioningPlan> plans = new ArrayList<>();
        for (DriverSnapshot driver : constraintProvider.eligibleDrivers(snapshot)) {
            createRepositioningPlan(driver, snapshot, demandByZone, mutableSupplyByZone)
                    .ifPresent(plans::add);
        }
        return plans;
    }

    private Optional<DriverRepositioningPlan> createRepositioningPlan(
            DriverSnapshot driver,
            OptimizationSnapshot snapshot,
            Map<ZoneId, Integer> demandByZone,
            Map<ZoneId, Integer> supplyByZone) {
        if (constraintProvider.wouldBreakCentralSupply(driver, supplyByZone)) {
            return Optional.empty();
        }
        ZoneId targetZone = findBestTargetZone(driver, snapshot, demandByZone, supplyByZone);
        if (targetZone == null || targetZone == driver.getCurrentZone()) {
            return Optional.empty();
        }
        moveSupply(driver.getCurrentZone(), targetZone, supplyByZone);
        return Optional.of(timefoldPlanningMapper.toRepositioningPlan(
                driver,
                targetZone,
                constraintProvider.priorityScore(
                        driver,
                        targetZone,
                        demandByZone,
                        snapshot.getDistanceMatrix()),
                calculateExpectedWaitReductionSeconds(driver, targetZone, snapshot),
                calculateExpectedCancellationReduction(snapshot)));
    }

    private int calculateComparisonScore(OptimizationComparison comparison) {
        return (int) Math.round(
                (comparison.getBaselineMetrics().getAverageWaitSeconds() - comparison.getOptimizedMetrics().getAverageWaitSeconds())
                        + (comparison.getBaselineMetrics().getCancellationRisk() - comparison.getOptimizedMetrics().getCancellationRisk()) * 100.0);
    }

    private ZoneId findBestTargetZone(DriverSnapshot driverSnapshot,
                                      OptimizationSnapshot snapshot,
                                      Map<ZoneId, Integer> demandByZone,
                                      Map<ZoneId, Integer> mutableSupplyByZone) {
        ZoneId bestZone = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (ZoneId zoneId : ZoneId.values()) {
            if (!constraintProvider.isReachable(driverSnapshot, zoneId, snapshot.getDistanceMatrix())) {
                continue;
            }
            if (constraintProvider.wouldOvercrowd(zoneId, mutableSupplyByZone, demandByZone)) {
                continue;
            }
            int shortage = calculateZoneShortage(zoneId, demandByZone, mutableSupplyByZone);
            if (shortage <= 0) {
                continue;
            }
            double score = calculateTargetZoneScore(
                    driverSnapshot,
                    zoneId,
                    demandByZone,
                    snapshot,
                    shortage);
            if (score > bestScore) {
                bestScore = score;
                bestZone = zoneId;
            }
        }
        return bestZone;
    }

    private int calculateZoneShortage(
            ZoneId zone,
            Map<ZoneId, Integer> demandByZone,
            Map<ZoneId, Integer> supplyByZone) {
        return demandByZone.getOrDefault(zone, 0) - supplyByZone.getOrDefault(zone, 0);
    }

    private double calculateTargetZoneScore(
            DriverSnapshot driver,
            ZoneId targetZone,
            Map<ZoneId, Integer> demandByZone,
            OptimizationSnapshot snapshot,
            int shortage) {
        double score = constraintProvider.priorityScore(
                driver,
                targetZone,
                demandByZone,
                snapshot.getDistanceMatrix()) + (shortage * 2.0);
        return targetZone == driver.getCurrentZone() ? score - 2.0 : score;
    }

    private void moveSupply(
            ZoneId currentZone,
            ZoneId targetZone,
            Map<ZoneId, Integer> supplyByZone) {
        supplyByZone.merge(currentZone, -1, Integer::sum);
        supplyByZone.merge(targetZone, 1, Integer::sum);
    }

    private int calculateExpectedWaitReductionSeconds(
            DriverSnapshot driver,
            ZoneId targetZone,
            OptimizationSnapshot snapshot) {
        return Math.max(
                10,
                snapshot.getDistanceMatrix().travelMinutes(driver.getCurrentZone(), targetZone) * 3);
    }

    private double calculateExpectedCancellationReduction(OptimizationSnapshot snapshot) {
        return Math.min(0.25, snapshot.getCancellationRisk() * 0.2);
    }

    private Map<ZoneId, Integer> calculateCurrentSupplyByZone(OptimizationSnapshot snapshot) {
        Map<ZoneId, Integer> supplyByZone = new EnumMap<>(ZoneId.class);
        for (ZoneId zoneId : ZoneId.values()) {
            supplyByZone.put(zoneId, 0);
        }
        snapshot.getDriverSnapshots().stream()
                .filter(DriverSnapshot::isRepositionable)
                .forEach(driver -> supplyByZone.merge(driver.getCurrentZone(), 1, Integer::sum));
        return supplyByZone;
    }
}
