package com.microgo.optimization_service.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationMetrics {
    private double averageWaitSeconds;
    private double p95WaitSeconds;
    private double cancellationRisk;
    private double highDemandCoverageRatio;
    private int unmetRequestCount;
    private int centralLondonSupplyCount;
}
