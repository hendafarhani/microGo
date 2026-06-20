package com.microgo.simulation_service.service.serviceimpl;

import com.microgo.simulation_service.domain.DriverAgent;
import com.microgo.simulation_service.domain.PassengerAgent;
import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.entity.SimulationRunEntity;
import com.microgo.simulation_service.kafka.publisher.impl.RideRequestPublisherImpl;
import com.microgo.simulation_service.mapper.PopulationEngineMapper;
import com.microgo.simulation_service.repository.DriverProfileRepository;
import com.microgo.simulation_service.repository.PassengerProfileRepository;
import com.microgo.simulation_service.service.DriverPopulationGenerator;
import com.microgo.simulation_service.service.PassengerPopulationGenerator;
import com.microgo.simulation_service.service.PopulationEngine;
import com.microgo.simulation_service.domain.PopulationSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PopulationEngineImpl implements PopulationEngine {

    private final PassengerPopulationGenerator passengerPopulationGenerator;
    private final DriverPopulationGenerator driverPopulationGenerator;
    private final PassengerProfileRepository passengerProfileRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final RideRequestPublisherImpl rideRequestPublisherImpl;

    @Override
    @Transactional
    public PopulationSnapshot bootstrapPopulation(ScenarioContext context, SimulationRunEntity runEntity) {
        List<PassengerAgent> generatedPassengers = passengerPopulationGenerator.generatePassengers(context);
        persistAndPublishPassengers(generatedPassengers, runEntity);

        List<DriverAgent> generatedDrivers = driverPopulationGenerator.generateDrivers(context);
        persistAndPublishDrivers(generatedDrivers, runEntity, context);

        return new PopulationSnapshot(generatedPassengers, generatedDrivers);
    }

    private void persistAndPublishPassengers(List<PassengerAgent> passengers, SimulationRunEntity runEntity) {
        for (PassengerAgent passenger : passengers) {
            passengerProfileRepository.save(PopulationEngineMapper.toPassengerProfileEntity(passenger, runEntity));
            // Persist first, then publish, so downstream consumers do not observe synthetic passengers
            // that lack durable profile state when a run is replayed or partially retried.
            rideRequestPublisherImpl.publishPassengerGenerated(PopulationEngineMapper.toPassengerGeneratedEvent(passenger));
        }
    }

    private void persistAndPublishDrivers(
            List<DriverAgent> drivers,
            SimulationRunEntity runEntity,
            ScenarioContext context) {
        for (DriverAgent driver : drivers) {
            driverProfileRepository.save(PopulationEngineMapper.toDriverProfileEntity(driver, runEntity));
            // Driver movement is owned elsewhere, so this publication acts as a handoff after profile creation.
            rideRequestPublisherImpl.publishDriverGenerated(PopulationEngineMapper.toDriverGeneratedEvent(driver, context));
        }
    }
}
