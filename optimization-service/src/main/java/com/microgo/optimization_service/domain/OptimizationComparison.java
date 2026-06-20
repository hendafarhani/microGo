package com.microgo.optimization_service.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OptimizationComparison {
    OptimizationMetrics baselineMetrics;
    OptimizationMetrics optimizedMetrics;
}
