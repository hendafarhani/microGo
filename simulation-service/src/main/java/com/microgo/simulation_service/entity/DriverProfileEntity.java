package com.microgo.simulation_service.entity;

import com.microgo.simulation_service.enums.ZoneId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "driver_profile")
public class DriverProfileEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "simulation_run_id", nullable = false)
    private SimulationRunEntity simulationRun;

    @Column(name = "external_driver_id", nullable = false, unique = true)
    private String externalDriverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "home_zone", nullable = false)
    private ZoneId homeZone;

    @Column(name = "fatigue_score", nullable = false)
    private double fatigueScore;

    @Column(name = "airport_preference", nullable = false)
    private double airportPreference;

    @Enumerated(EnumType.STRING)
    @Column(name = "destination_bias", nullable = false)
    private ZoneId destinationBias;

    @Column(name = "reliability_score", nullable = false)
    private double reliabilityScore;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
