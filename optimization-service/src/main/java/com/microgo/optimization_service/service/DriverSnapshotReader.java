package com.microgo.optimization_service.service;

import com.microgo.optimization_service.domain.DriverSnapshot;
import com.microgo.optimization_service.enums.ScenarioType;
import com.microgo.optimization_service.kafka.model.DriverLocationUpdatedEvent;

import java.util.List;

public interface DriverSnapshotReader {

    void onDriverLocationUpdated(DriverLocationUpdatedEvent event);

    List<DriverSnapshot> findCurrentDrivers(ScenarioType activeScenario);
}
