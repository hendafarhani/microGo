package com.microgo.simulation_service.businessrule;

import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.domain.SimulationMetricsSnapshot;
import com.microgo.simulation_service.enums.ScenarioType;
import com.microgo.simulation_service.enums.ZoneId;

import java.util.EnumMap;
import java.util.Map;

public final class DemandForecastBusinessRules {

    private DemandForecastBusinessRules() {
    }

    public static Map<ZoneId, Integer> buildPredictedDemandByZone(
            ScenarioContext context,
            SimulationMetricsSnapshot snapshot) {
        Map<ZoneId, Integer> demandByZone = new EnumMap<>(ZoneId.class);
        demandByZone.put(context.getAnchorZone(), minimumDemand(snapshot.getPendingRideRequests()));
        if (context.getScenario() == ScenarioType.AIRPORT_RUSH) {
            addAirportRushDemandShape(snapshot, demandByZone);
            return demandByZone;
        }
        addConcertDemandShape(snapshot, demandByZone);
        return demandByZone;
    }

    // This forecast is intentionally heuristic: we fan pending demand into a few scenario-relevant zones
    // so the optimization consumer gets a stable, interpretable demand shape instead of a flat count.
    private static void addAirportRushDemandShape(
            SimulationMetricsSnapshot snapshot,
            Map<ZoneId, Integer> demandByZone) {
        demandByZone.put(ZoneId.CENTRAL_LONDON, minimumDemand(snapshot.getPendingRideRequests() / 2));
        demandByZone.put(ZoneId.HEATHROW_CORRIDOR, minimumDemand(snapshot.getPendingRideRequests()));
    }

    private static void addConcertDemandShape(
            SimulationMetricsSnapshot snapshot,
            Map<ZoneId, Integer> demandByZone) {
        demandByZone.put(ZoneId.WEMBLEY_EVENT_ZONE, minimumDemand(snapshot.getPendingRideRequests()));
        demandByZone.put(ZoneId.GENERAL_LONDON, minimumDemand(snapshot.getPendingRideRequests() / 3));
    }

    private static int minimumDemand(int demand) {
        return Math.max(1, demand);
    }
}

