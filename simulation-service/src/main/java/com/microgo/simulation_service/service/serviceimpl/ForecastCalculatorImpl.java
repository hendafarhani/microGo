package com.microgo.simulation_service.service.serviceimpl;

import com.microgo.simulation_service.domain.OptimizationSnapshot;
import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.domain.SimulationMetricsSnapshot;
import com.microgo.simulation_service.enums.ZoneId;
import com.microgo.simulation_service.mapper.OptimizationSnapshotMapper;
import com.microgo.simulation_service.service.ForecastCalculator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

@Service
public class ForecastCalculatorImpl implements ForecastCalculator {

    @Override
    public OptimizationSnapshot calculate(ScenarioContext context, SimulationMetricsSnapshot snapshot) {
        Map<ZoneId, Integer> predictedDemandByZone = buildPredictedDemandByZone(context, snapshot);
        return OptimizationSnapshotMapper.toOptimizationSnapshot(
                context,
                snapshot,
                predictedDemandByZone,
                Instant.now());
    }

    private Map<ZoneId, Integer> buildPredictedDemandByZone(
            ScenarioContext context,
            SimulationMetricsSnapshot snapshot) {
        Map<ZoneId, Integer> demandByZone = new EnumMap<>(ZoneId.class);
        demandByZone.put(context.getAnchorZone(), minimumDemand(snapshot.getPendingRideRequests()));
        if (isAirportRush(context)) {
            addAirportRushDemandShape(snapshot, demandByZone);
            return demandByZone;
        }
        addConcertDemandShape(snapshot, demandByZone);
        return demandByZone;
    }

    // This forecast is intentionally heuristic: we fan pending demand into a few scenario-relevant zones
    // so the optimization consumer gets a stable, interpretable demand shape instead of a flat count.
    private void addAirportRushDemandShape(
            SimulationMetricsSnapshot snapshot,
            Map<ZoneId, Integer> demandByZone) {
        demandByZone.put(ZoneId.CENTRAL_LONDON, minimumDemand(snapshot.getPendingRideRequests() / 2));
        demandByZone.put(ZoneId.HEATHROW_CORRIDOR, minimumDemand(snapshot.getPendingRideRequests()));
    }

    private void addConcertDemandShape(
            SimulationMetricsSnapshot snapshot,
            Map<ZoneId, Integer> demandByZone) {
        demandByZone.put(ZoneId.WEMBLEY_EVENT_ZONE, minimumDemand(snapshot.getPendingRideRequests()));
        demandByZone.put(ZoneId.GENERAL_LONDON, minimumDemand(snapshot.getPendingRideRequests() / 3));
    }

    private boolean isAirportRush(ScenarioContext context) {
        return context.getScenario().name().equals("AIRPORT_RUSH");
    }

    private int minimumDemand(int demand) {
        return Math.max(1, demand);
    }
}
