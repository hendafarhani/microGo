package com.microgo.dashboard_service.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.microgo.dashboard_service.entity.EventOutboxEntity;
import com.microgo.dashboard_service.enums.RideRequestEventType;

public record ResolvedDashboardEvent(
        EventOutboxEntity outboxEvent,
        RideRequestEventType eventType,
        JsonNode payload
) {
}
