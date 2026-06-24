package com.microgo.simulation_service.entity;

import com.microgo.simulation_service.enums.ScenarioType;
import com.microgo.simulation_service.enums.TrafficLevel;
import com.microgo.simulation_service.enums.WeatherCondition;
import com.microgo.simulation_service.enums.ZoneId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "scenario_config")
public class ScenarioConfigEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "scenario_name", nullable = false, unique = true)
    private ScenarioType scenarioName;

    @Column(nullable = false)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(name = "anchor_zone", nullable = false)
    private ZoneId anchorZone;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WeatherCondition weather;

    @Enumerated(EnumType.STRING)
    @Column(name = "traffic_level", nullable = false)
    private TrafficLevel trafficLevel;

    @Column(name = "passenger_demand_multiplier", nullable = false)
    private double passengerDemandMultiplier;

    @Column(name = "cancellation_risk_multiplier", nullable = false)
    private double cancellationRiskMultiplier;

    @Column(name = "driver_speed_multiplier", nullable = false)
    private double driverSpeedMultiplier;

    @Column(name = "airport_fare_bias", nullable = false)
    private double airportFareBias;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
