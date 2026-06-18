package com.microgo.dashboard_service.service.serviceimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microgo.dashboard_service.model.RideDashboardMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardStreamingServiceImplTest {

    private RecordingSimpMessagingTemplate messagingTemplate;
    private DashboardStreamingServiceImpl dashboardStreamingService;

    @BeforeEach
    void setUp() {
        messagingTemplate = new RecordingSimpMessagingTemplate();
        dashboardStreamingService = new DashboardStreamingServiceImpl(messagingTemplate);
    }

    @Test
    void streamsRideEventToRideSpecificDestination() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("rideStatus", "PENDING");

        RideDashboardMessage message = new RideDashboardMessage(
                7L,
                "REQUEST_CREATED",
                "ride-7",
                "user-7",
                null,
                "PENDING",
                "RIDE_REQUEST",
                payload,
                payload
        );

        dashboardStreamingService.streamRideEvent(message);

        assertThat(messagingTemplate.destination).isEqualTo("/topic/ride-requests/ride-7");
        assertThat(messagingTemplate.payload).isEqualTo(message);
    }

    private static final class RecordingSimpMessagingTemplate extends SimpMessagingTemplate {
        private String destination;
        private Object payload;

        private RecordingSimpMessagingTemplate() {
            super(new NoOpMessageChannel());
        }

        @Override
        public void convertAndSend(String destination, Object payload) {
            this.destination = destination;
            this.payload = payload;
        }
    }

    private static final class NoOpMessageChannel implements MessageChannel {
        @Override
        public boolean send(Message<?> message) {
            return true;
        }

        @Override
        public boolean send(Message<?> message, long timeout) {
            return true;
        }
    }
}
