package com.microgo.optimization_service.mapper;

import com.microgo.optimization_service.domain.OptimizationComparison;
import com.microgo.optimization_service.domain.OptimizationMetrics;
import org.springframework.stereotype.Component;

@Component
public class OptimizationComparisonMapper {

    public OptimizationComparison toComparison(
            OptimizationMetrics baselineMetrics,
            OptimizationMetrics optimizedMetrics) {
        return OptimizationComparison.builder()
                .baselineMetrics(baselineMetrics)
                .optimizedMetrics(optimizedMetrics)
                .build();
    }

    public OptimizationMetrics toMetrics(
            double averageWaitSeconds,
            double p95WaitSeconds,
            double cancellationRisk,
            double highDemandCoverageRatio,
            int unmetRequestCount,
            int centralLondonSupplyCount) {
        return OptimizationMetrics.builder()
                .averageWaitSeconds(averageWaitSeconds)
                .p95WaitSeconds(p95WaitSeconds)
                .cancellationRisk(cancellationRisk)
                .highDemandCoverageRatio(highDemandCoverageRatio)
                .unmetRequestCount(unmetRequestCount)
                .centralLondonSupplyCount(centralLondonSupplyCount)
                .build();
    }
}
