package com.microgo.dashboard_service.kafka;

import com.microgo.dashboard_service.kafka.handler.DashboardEventHandler;
import com.microgo.dashboard_service.service.DashboardEventProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DashboardEventHandlerTest {

    private RecordingDashboardEventProcessor dashboardEventProcessor;
    private DashboardEventHandler dashboardEventHandler;

    @BeforeEach
    void setUp() {
        dashboardEventProcessor = new RecordingDashboardEventProcessor();
        dashboardEventHandler = new DashboardEventHandler(dashboardEventProcessor);
    }

    @Test
    void listenDelegatesMessageProcessing() {
        String payload = "{\"eventId\":1}";

        dashboardEventHandler.listen(payload);

        org.assertj.core.api.Assertions.assertThat(dashboardEventProcessor.lastMessage).isEqualTo(payload);
    }

    private static final class RecordingDashboardEventProcessor implements DashboardEventProcessor {
        private String lastMessage;

        @Override
        public boolean process(String message) {
            this.lastMessage = message;
            return true;
        }
    }
}
