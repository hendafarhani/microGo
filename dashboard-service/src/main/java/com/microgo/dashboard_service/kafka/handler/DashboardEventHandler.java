package com.microgo.dashboard_service.kafka.handler;

import com.microgo.dashboard_service.service.DashboardEventProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DashboardEventHandler {

    private final DashboardEventProcessor dashboardEventProcessor;

    @KafkaListener(
            id = "${dashboard.service.listener-id}",
            topics = "${dashboard.service.event-topic}",
            groupId = "${dashboard.service.consumer-group-id}",
            containerFactory = "dashboardEventListenerContainerFactory"
    )
    public void listen(String message) {
        dashboardEventProcessor.process(message);
    }
}
