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
@Table(name = "passenger_profile")
public class PassengerProfileEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "simulation_run_id", nullable = false)
    private SimulationRunEntity simulationRun;

    @Column(name = "external_passenger_id", nullable = false, unique = true)
    private String externalPassengerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin_zone", nullable = false)
    private ZoneId originZone;

    @Enumerated(EnumType.STRING)
    @Column(name = "destination_zone", nullable = false)
    private ZoneId destinationZone;

    @Column(name = "urgency_score", nullable = false)
    private double urgencyScore;

    @Column(name = "cancellation_sensitivity", nullable = false)
    private double cancellationSensitivity;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
