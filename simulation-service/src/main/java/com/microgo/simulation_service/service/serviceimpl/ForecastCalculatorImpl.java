package com.microgo.simulation_service.service.serviceimpl;

import com.microgo.simulation_service.businessrule.DemandForecastBusinessRules;
import com.microgo.simulation_service.domain.OptimizationSnapshot;
import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.domain.SimulationMetricsSnapshot;
import com.microgo.simulation_service.enums.ZoneId;
import com.microgo.simulation_service.mapper.OptimizationSnapshotMapper;
import com.microgo.simulation_service.service.ForecastCalculator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class ForecastCalculatorImpl implements ForecastCalculator {

    @Override
    public OptimizationSnapshot calculate(ScenarioContext context, SimulationMetricsSnapshot snapshot) {
        Map<ZoneId, Integer> predictedDemandByZone = DemandForecastBusinessRules.buildPredictedDemandByZone(context, snapshot);
        return OptimizationSnapshotMapper.toOptimizationSnapshot(
                context,
                snapshot,
                predictedDemandByZone,
                Instant.now());
    }
}
