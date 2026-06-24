package com.microgo.optimization_service.service;

import com.microgo.optimization_service.domain.OptimizationComparison;
import com.microgo.optimization_service.domain.OptimizationMetrics;
import com.microgo.optimization_service.domain.OptimizationSnapshot;
import com.microgo.optimization_service.enums.ZoneId;
import com.microgo.optimization_service.domain.RideAvailabilityOptimizationSolution;

import java.util.List;
import java.util.Map;

public interface BaselineVsOptimizedComparator {

    OptimizationComparison compareBaselineWithOptimized(
            OptimizationSnapshot snapshot,
            RideAvailabilityOptimizationSolution solution);

    OptimizationMetrics calculateMetrics(OptimizationSnapshot snapshot, Map<String, ZoneId> targetZones);

    List<String> findBaselineNearestDriverIds(OptimizationSnapshot snapshot, ZoneId pickupZone, int limit);
}
