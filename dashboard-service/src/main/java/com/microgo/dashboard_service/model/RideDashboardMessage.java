package com.microgo.dashboard_service.model;

import com.fasterxml.jackson.databind.JsonNode;

public record RideDashboardMessage(
        Long eventId,
        String eventType,
        String rideRequestIdentifier,
        String requesterId,
        String riderId,
        String rideStatus,
        String sourceTable,
        JsonNode payload,
        JsonNode data
) {
}
