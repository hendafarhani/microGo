package com.microgo.simulation_service.mapper;

import com.microgo.simulation_service.domain.DriverAgent;
import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.enums.ScenarioType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PopulationEngineMapperTest {

    @Test
    void includesStableDisplayIdInDriverGeneratedEvent() {
        DriverAgent driver = DriverAgent.builder()
                .driverId("driver london 101")
                .build();
        ScenarioContext context = ScenarioContext.builder()
                .scenario(ScenarioType.AIRPORT_RUSH)
                .build();

        var event = PopulationEngineMapper.toDriverGeneratedEvent(driver, context);

        assertThat(event.getDriverId()).isEqualTo("driver london 101");
        assertThat(event.getDriverDisplayId()).isEqualTo("DRV-DRIVER-LONDON-101");
        assertThat(event.getScenario()).isEqualTo(ScenarioType.AIRPORT_RUSH);
    }
}
