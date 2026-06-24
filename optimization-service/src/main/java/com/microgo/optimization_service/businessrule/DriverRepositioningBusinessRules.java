package com.microgo.optimization_service.businessrule;

import com.microgo.optimization_service.domain.DriverSnapshot;
import com.microgo.optimization_service.domain.OptimizationComparison;
import com.microgo.optimization_service.domain.OptimizationSnapshot;
import com.microgo.optimization_service.enums.ZoneId;

public final class DriverRepositioningBusinessRules {

    private static final double SHORTAGE_SCORE_WEIGHT = 2.0;
    private static final double SAME_ZONE_PENALTY = 2.0;
    private static final int MINIMUM_WAIT_REDUCTION_SECONDS = 10;
    private static final int WAIT_REDUCTION_PER_TRAVEL_MINUTE = 3;
    private static final double MAX_CANCELLATION_REDUCTION = 0.25;
    private static final double CANCELLATION_REDUCTION_FACTOR = 0.2;
    private static final double CANCELLATION_SCORE_WEIGHT = 100.0;

    private DriverRepositioningBusinessRules() {
    }

    public static int calculateComparisonScore(OptimizationComparison comparison) {
        return (int) Math.round(
                (comparison.getBaselineMetrics().getAverageWaitSeconds()
                        - comparison.getOptimizedMetrics().getAverageWaitSeconds())
                        + (comparison.getBaselineMetrics().getCancellationRisk()
                        - comparison.getOptimizedMetrics().getCancellationRisk()) * CANCELLATION_SCORE_WEIGHT);
    }

    public static double calculateTargetZoneScore(
            double basePriorityScore,
            int shortage,
            boolean sameZoneAsDriver) {
        double score = basePriorityScore + (shortage * SHORTAGE_SCORE_WEIGHT);
        return sameZoneAsDriver ? score - SAME_ZONE_PENALTY : score;
    }

    public static int calculateExpectedWaitReductionSeconds(
            DriverSnapshot driver,
            ZoneId targetZone,
            OptimizationSnapshot snapshot) {
        return Math.max(
                MINIMUM_WAIT_REDUCTION_SECONDS,
                snapshot.getDistanceMatrix().travelMinutes(driver.getCurrentZone(), targetZone)
                        * WAIT_REDUCTION_PER_TRAVEL_MINUTE);
    }

    public static double calculateExpectedCancellationReduction(OptimizationSnapshot snapshot) {
        return Math.min(MAX_CANCELLATION_REDUCTION, snapshot.getCancellationRisk() * CANCELLATION_REDUCTION_FACTOR);
    }
}

