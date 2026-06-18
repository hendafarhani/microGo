package com.microgo.dashboard_service.service;

public interface DashboardAckPublisher {

    void publishWebsocketPublished(Long eventId);
}
