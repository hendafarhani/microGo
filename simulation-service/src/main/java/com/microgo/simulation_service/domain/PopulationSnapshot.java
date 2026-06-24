package com.microgo.simulation_service.domain;

import java.util.List;

public record PopulationSnapshot(List<PassengerAgent> passengers, List<DriverAgent> drivers) {
}
