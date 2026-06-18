package com.microgo.dashboard_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "EVENT_OUTBOX")
public class EventOutboxEntity {

    @Id
    private Long id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "ride_request_id", nullable = false)
    private Long rideRequestId;

    @Column(name = "ride_request_identifier", nullable = false)
    private String rideRequestIdentifier;

    @Column(name = "requester_id", nullable = false)
    private String requesterId;

    @Column(name = "rider_id")
    private String riderId;

    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "LONGTEXT")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;
}
