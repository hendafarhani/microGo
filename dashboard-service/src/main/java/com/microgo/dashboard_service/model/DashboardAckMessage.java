package com.microgo.dashboard_service.model;

import java.time.OffsetDateTime;

public record DashboardAckMessage(
        Long eventId,
        String status,
        OffsetDateTime processedAt,
        String service
) {
}
