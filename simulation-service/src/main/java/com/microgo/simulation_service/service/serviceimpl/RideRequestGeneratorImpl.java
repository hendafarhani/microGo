package com.microgo.simulation_service.service.serviceimpl;

import com.microgo.simulation_service.domain.PassengerAgent;
import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.kafka.model.SimulatedRideRequestedEvent;
import com.microgo.simulation_service.mapper.SimulatedRideRequestedEventMapper;
import com.microgo.simulation_service.service.RideRequestGenerator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RideRequestGeneratorImpl implements RideRequestGenerator {

    @Override
    public List<SimulatedRideRequestedEvent> generateRideRequests(
            ScenarioContext context,
            List<PassengerAgent> passengers) {
        return passengers.stream()
                .map(passenger -> toRideRequestedEvent(context, passenger))
                .toList();
    }

    private SimulatedRideRequestedEvent toRideRequestedEvent(
            ScenarioContext context,
            PassengerAgent passenger) {
        return SimulatedRideRequestedEventMapper.toRideRequestedEvent(
                context,
                passenger,
                buildRideId(passenger),
                Instant.now());
    }

    private String buildRideId(PassengerAgent passenger) {
        return "ride-" + passenger.getPassengerId();
    }
}
