package com.microgo.simulation_service.mapper;

import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.entity.ScenarioConfigEntity;
import com.microgo.simulation_service.entity.SimulationRunEntity;

public final class ScenarioContextMapper {

    private ScenarioContextMapper() {
        // Private constructor to prevent instantiation
    }

    public static ScenarioContext toScenarioContext(
            SimulationRunEntity runEntity,
            ScenarioConfigEntity entity) {
        return ScenarioContext.builder()
                .simulationRunId(runEntity.getId())
                .scenario(entity.getScenarioName())
                .anchorZone(entity.getAnchorZone())
                .startTime(entity.getStartTime())
                .weather(entity.getWeather())
                .trafficLevel(entity.getTrafficLevel())
                .passengerDemandMultiplier(entity.getPassengerDemandMultiplier())
                .cancellationRiskMultiplier(entity.getCancellationRiskMultiplier())
                .driverSpeedMultiplier(entity.getDriverSpeedMultiplier())
                .airportFareBias(entity.getAirportFareBias())
                .startedAt(runEntity.getStartedAt())
                .build();
    }
}
