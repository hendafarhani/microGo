package com.microgo.simulation_service.mapper;

import com.microgo.simulation_service.domain.DriverAgent;
import com.microgo.simulation_service.domain.PassengerAgent;
import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.entity.DriverProfileEntity;
import com.microgo.simulation_service.entity.PassengerProfileEntity;
import com.microgo.simulation_service.entity.SimulationRunEntity;
import com.microgo.simulation_service.kafka.model.DriverGeneratedEvent;
import com.microgo.simulation_service.kafka.model.PassengerGeneratedEvent;

public final class PopulationEngineMapper {

    private PopulationEngineMapper() {
        // Private constructor to prevent instantiation
    }

    public static PassengerProfileEntity toPassengerProfileEntity(
            PassengerAgent passenger,
            SimulationRunEntity runEntity) {
        return PassengerProfileEntity.builder()
                .id(passenger.getId())
                .simulationRun(runEntity)
                .externalPassengerId(passenger.getPassengerId())
                .originZone(passenger.getOriginZone())
                .destinationZone(passenger.getDestinationZone())
                .urgencyScore(passenger.getUrgencyScore())
                .cancellationSensitivity(passenger.getCancellationSensitivity())
                .createdAt(passenger.getCreatedAt())
                .build();
    }

    public static PassengerGeneratedEvent toPassengerGeneratedEvent(PassengerAgent passenger) {
        return PassengerGeneratedEvent.builder()
                .passengerId(passenger.getPassengerId())
                .simulationRunId(passenger.getSimulationRunId())
                .originZone(passenger.getOriginZone())
                .destinationZone(passenger.getDestinationZone())
                .urgencyScore(passenger.getUrgencyScore())
                .createdAt(passenger.getCreatedAt())
                .build();
    }

    public static DriverProfileEntity toDriverProfileEntity(
            DriverAgent driver,
            SimulationRunEntity runEntity) {
        return DriverProfileEntity.builder()
                .id(driver.getId())
                .simulationRun(runEntity)
                .externalDriverId(driver.getDriverId())
                .homeZone(driver.getHomeZone())
                .fatigueScore(driver.getFatigueScore())
                .airportPreference(driver.getAirportPreference())
                .destinationBias(driver.getDestinationBias())
                .reliabilityScore(driver.getReliabilityScore())
                .createdAt(driver.getCreatedAt())
                .build();
    }

    public static DriverGeneratedEvent toDriverGeneratedEvent(
            DriverAgent driver,
            ScenarioContext context) {
        return DriverGeneratedEvent.builder()
                .driverId(driver.getDriverId())
                .driverDisplayId(toDriverDisplayId(driver.getDriverId()))
                .scenario(context.getScenario())
                .build();
    }

    private static String toDriverDisplayId(String driverIdentifier) {
        return "DRV-" + driverIdentifier.toUpperCase().replaceAll("[^A-Z0-9]+", "-");
    }
}
