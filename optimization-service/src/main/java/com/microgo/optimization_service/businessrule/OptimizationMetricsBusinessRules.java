package com.microgo.optimization_service.businessrule;

import com.microgo.optimization_service.domain.OptimizationMetrics;
import com.microgo.optimization_service.domain.OptimizationSnapshot;
import com.microgo.optimization_service.enums.ZoneId;

import java.util.Map;

public final class OptimizationMetricsBusinessRules {

    private static final double WAIT_BASE_MULTIPLIER = 0.8;
    private static final double WAIT_SHORTAGE_PENALTY_SECONDS = 18.0;
    private static final double P95_SHORTAGE_PENALTY_SECONDS = 45.0;
    private static final double CANCELLATION_BASE_MULTIPLIER = 0.82;
    private static final double HIGH_DEMAND_COVERAGE_DISCOUNT = 0.25;
    private static final double CANCELLATION_CAP_FACTOR = 1.05;

    private OptimizationMetricsBusinessRules() {
    }

    public static OptimizationMetrics calculateMetrics(
            OptimizationSnapshot snapshot,
            Map<ZoneId, Integer> demandByZone,
            Map<ZoneId, Integer> supplyByZone) {
        int unmetRequestCount = ZoneDemandBusinessRules.countUnmetRequests(demandByZone, supplyByZone);
        int totalDemand = ZoneDemandBusinessRules.totalDemand(demandByZone);
        double shortageRatio = unmetRequestCount / (double) totalDemand;
        int maximumZoneShortage = ZoneDemandBusinessRules.maximumZoneShortage(demandByZone, supplyByZone);
        double highDemandCoverageRatio = ZoneDemandBusinessRules.highDemandCoverageRatio(demandByZone, supplyByZone);
        double averageWaitSeconds = snapshot.getAverageWaitingTimeSeconds() * (WAIT_BASE_MULTIPLIER + shortageRatio)
                + (maximumZoneShortage * WAIT_SHORTAGE_PENALTY_SECONDS);
        double p95WaitSeconds = averageWaitSeconds + (maximumZoneShortage * P95_SHORTAGE_PENALTY_SECONDS);
        double cancellationRisk = clamp(snapshot.getCancellationRisk() * (CANCELLATION_BASE_MULTIPLIER + shortageRatio)
                * (CANCELLATION_CAP_FACTOR - (highDemandCoverageRatio * HIGH_DEMAND_COVERAGE_DISCOUNT)));

        return OptimizationMetrics.builder()
                .averageWaitSeconds(averageWaitSeconds)
                .p95WaitSeconds(p95WaitSeconds)
                .cancellationRisk(cancellationRisk)
                .highDemandCoverageRatio(highDemandCoverageRatio)
                .unmetRequestCount(unmetRequestCount)
                .centralLondonSupplyCount(supplyByZone.getOrDefault(ZoneId.CENTRAL_LONDON, 0))
                .build();
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}

