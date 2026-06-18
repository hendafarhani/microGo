package com.microgo.dashboard_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "RIDE_REQUEST_DRIVER_OFFER")
public class RideRequestDriverOfferEntity {

    @Id
    private Long id;

    @Column(name = "ride_request_id", nullable = false)
    private Long rideRequestId;

    @Column(name = "rider_id", nullable = false)
    private Long riderId;

    @Column(name = "notification_round", nullable = false)
    private int notificationRound;

    @Column(name = "notified_at", nullable = false)
    private OffsetDateTime notifiedAt;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "responded_at")
    private OffsetDateTime respondedAt;
}
