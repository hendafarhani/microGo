package com.microgo.dashboard_service.mapper;

import com.microgo.dashboard_service.model.DashboardAckMessage;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class DashboardAckMessageMapper {

    private static final String WEBSOCKET_PUBLISHED = "WEBSOCKET_PUBLISHED";

    public DashboardAckMessage mapWebsocketPublished(Long eventId, String serviceName) {
        return new DashboardAckMessage(
                eventId,
                WEBSOCKET_PUBLISHED,
                OffsetDateTime.now(),
                serviceName
        );
    }
}
