package com.microgo.simulation_service.service;

import com.microgo.simulation_service.domain.PassengerAgent;
import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.kafka.model.SimulatedRideRequestedEvent;

import java.util.List;

public interface RideRequestGenerator {

    List<SimulatedRideRequestedEvent> generateRideRequests(
            ScenarioContext context,
            List<PassengerAgent> passengers);
}
