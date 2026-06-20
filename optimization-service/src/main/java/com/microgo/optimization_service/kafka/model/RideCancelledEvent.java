package com.microgo.optimization_service.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideCancelledEvent {
    private String rideId;
    private String driverId;
    private String passengerId;
    private String reason;
    private Instant occurredAt;
}
