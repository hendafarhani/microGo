package com.microgo.simulation_service.kafka.model;

import com.microgo.simulation_service.enums.ScenarioType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverGeneratedEvent {
    private String driverId;
    private String driverDisplayId;
    private ScenarioType scenario;
}
