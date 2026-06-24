package com.microgo.optimization_service.businessrule;

import com.microgo.optimization_service.enums.ScenarioType;
import com.microgo.optimization_service.enums.ZoneId;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SimulationDemandBusinessRulesTest {

    @Test
    void shouldInferAirportRushDemandShapeAndTrafficMultiplier() {
        Map<ZoneId, Integer> demandByZone = SimulationDemandBusinessRules.inferDemandByScenario(
                ScenarioType.AIRPORT_RUSH,
                8);

        assertThat(demandByZone)
                .containsEntry(ZoneId.HEATHROW_CORRIDOR, 8)
                .containsEntry(ZoneId.CENTRAL_LONDON, 4)
                .containsEntry(ZoneId.GENERAL_LONDON, 2)
                .containsEntry(ZoneId.WEMBLEY_EVENT_ZONE, 0);
        assertThat(SimulationDemandBusinessRules.resolveTrafficMultiplier(ScenarioType.AIRPORT_RUSH, 0.78, 0.84))
                .isEqualTo(0.84);
    }

    @Test
    void shouldKeepMinimumConcertDemandForZeroPendingRequests() {
        Map<ZoneId, Integer> demandByZone = SimulationDemandBusinessRules.inferDemandByScenario(
                ScenarioType.CONCERT_RAIN,
                0);

        assertThat(demandByZone)
                .containsEntry(ZoneId.WEMBLEY_EVENT_ZONE, 1)
                .containsEntry(ZoneId.CENTRAL_LONDON, 1)
                .containsEntry(ZoneId.GENERAL_LONDON, 1)
                .containsEntry(ZoneId.HEATHROW_CORRIDOR, 0);
    }
}

