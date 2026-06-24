package com.microgo.optimization_service.service;

import com.microgo.optimization_service.domain.OptimizationSnapshot;
import com.microgo.optimization_service.domain.RideAvailabilityOptimizationSolution;

public interface TimefoldSolverService {

    RideAvailabilityOptimizationSolution solveSnapshot(OptimizationSnapshot snapshot);
}
