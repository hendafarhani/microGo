package com.microgo.dashboard_service.model;

import com.fasterxml.jackson.databind.JsonNode;

public record DashboardProjection(
        String sourceTable,
        JsonNode data
) {
}
