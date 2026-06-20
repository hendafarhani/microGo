package com.microgo.driver_location_streamer.service.serviceimpl;

import com.microgo.driver_location_streamer.model.DriverLocationUpdatedEvent;
import com.microgo.driver_location_streamer.service.DriverLocationStreamingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.ExecutorSubscribableChannel;

import static org.assertj.core.api.Assertions.assertThat;

class DriverLocationStreamingServiceImplTest {

    private RecordingSimpMessagingTemplate messagingTemplate;
    private DriverLocationStreamingServiceImpl service;

    @BeforeEach
    void setUp() {
        messagingTemplate = new RecordingSimpMessagingTemplate();
        service = new DriverLocationStreamingServiceImpl(messagingTemplate);
    }

    @Test
    void streamDriverLocationBroadcastsFullEventPayload() {
        DriverLocationUpdatedEvent event = DriverLocationUpdatedEvent.builder()
                .driverId("driver-1")
                .providerIdentifier("driver-1")
                .status("CRUISING")
                .latitude(51.5074)
                .longitude(-0.1278)
                .build();

        service.streamDriverLocation(event);

        assertThat(messagingTemplate.destination).isEqualTo(DriverLocationStreamingService.DRIVER_LOCATIONS_DESTINATION);
        assertThat(messagingTemplate.payload).isEqualTo(event);
    }

    private static class RecordingSimpMessagingTemplate extends SimpMessagingTemplate {
        private String destination;
        private Object payload;

        private RecordingSimpMessagingTemplate() {
            super(new ExecutorSubscribableChannel());
        }

        @Override
        public void convertAndSend(String destination, Object payload) {
            this.destination = destination;
            this.payload = payload;
        }
    }
}
