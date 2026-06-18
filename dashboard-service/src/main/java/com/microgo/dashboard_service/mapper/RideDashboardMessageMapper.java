package com.microgo.dashboard_service.mapper;

import com.microgo.dashboard_service.entity.EventOutboxEntity;
import com.microgo.dashboard_service.model.DashboardProjection;
import com.microgo.dashboard_service.model.ResolvedDashboardEvent;
import com.microgo.dashboard_service.model.RideDashboardMessage;
import org.springframework.stereotype.Component;

@Component
public class RideDashboardMessageMapper {

    public RideDashboardMessage map(ResolvedDashboardEvent resolvedEvent, DashboardProjection projection) {
        EventOutboxEntity outboxEvent = resolvedEvent.outboxEvent();

        return new RideDashboardMessage(
                outboxEvent.getId(),
                outboxEvent.getEventType(),
                outboxEvent.getRideRequestIdentifier(),
                outboxEvent.getRequesterId(),
                outboxEvent.getRiderId(),
                resolvedEvent.payload().path("rideStatus").asText(null),
                projection.sourceTable(),
                resolvedEvent.payload(),
                projection.data()
        );
    }
}
