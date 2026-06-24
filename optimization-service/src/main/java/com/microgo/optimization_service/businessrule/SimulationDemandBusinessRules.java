package com.microgo.optimization_service.businessrule;

import com.microgo.optimization_service.enums.ScenarioType;
import com.microgo.optimization_service.enums.ZoneId;

import java.util.EnumMap;
import java.util.Map;

public final class SimulationDemandBusinessRules {

    private SimulationDemandBusinessRules() {
    }

    public static double resolveTrafficMultiplier(
            ScenarioType scenarioType,
            double concertRainTrafficMultiplier,
            double airportRushTrafficMultiplier) {
        return switch (scenarioType) {
            case CONCERT_RAIN -> concertRainTrafficMultiplier;
            case AIRPORT_RUSH -> airportRushTrafficMultiplier;
        };
    }

    public static Map<ZoneId, Integer> inferDemandByScenario(ScenarioType scenarioType, int pendingRideRequests) {
        Map<ZoneId, Integer> demandByZone = new EnumMap<>(ZoneId.class);
        if (scenarioType == ScenarioType.AIRPORT_RUSH) {
            demandByZone.put(ZoneId.HEATHROW_CORRIDOR, minimumDemand(pendingRideRequests));
            demandByZone.put(ZoneId.CENTRAL_LONDON, minimumDemand(pendingRideRequests / 2));
            demandByZone.put(ZoneId.GENERAL_LONDON, minimumDemand(pendingRideRequests / 4));
            demandByZone.put(ZoneId.WEMBLEY_EVENT_ZONE, 0);
            return demandByZone;
        }

        demandByZone.put(ZoneId.WEMBLEY_EVENT_ZONE, minimumDemand(pendingRideRequests));
        demandByZone.put(ZoneId.CENTRAL_LONDON, minimumDemand(pendingRideRequests / 3));
        demandByZone.put(ZoneId.GENERAL_LONDON, minimumDemand(pendingRideRequests / 4));
        demandByZone.put(ZoneId.HEATHROW_CORRIDOR, 0);
        return demandByZone;
    }

    private static int minimumDemand(int demand) {
        return Math.max(1, demand);
    }
}

