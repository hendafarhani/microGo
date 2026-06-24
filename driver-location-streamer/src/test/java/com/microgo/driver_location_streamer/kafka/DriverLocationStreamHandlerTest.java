package com.microgo.driver_location_streamer.kafka;

import com.microgo.driver_location_streamer.kafka.handler.DriverLocationStreamHandler;
import com.microgo.driver_location_streamer.model.DriverLocationUpdatedEvent;
import com.microgo.driver_location_streamer.service.DriverLocationStreamingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DriverLocationStreamHandlerTest {

    private RecordingStreamingService streamingService;
    private DriverLocationStreamHandler handler;

    @BeforeEach
    void setUp() {
        streamingService = new RecordingStreamingService();
        handler = new DriverLocationStreamHandler(streamingService);
    }

    @Test
    void listenBroadcastsDriverLocationEvent() {
        DriverLocationUpdatedEvent event = DriverLocationUpdatedEvent.builder()
                .driverId("driver-1")
                .providerIdentifier("driver-1")
                .status("CRUISING")
                .latitude(51.5074)
                .longitude(-0.1278)
                .build();

        handler.listen(event.getDriverId(), event);

        assertThat(streamingService.lastEvent).isEqualTo(event);
    }

    @Test
    void listenBackfillsProviderIdentifierFromDriverIdWhenMissing() {
        DriverLocationUpdatedEvent event = DriverLocationUpdatedEvent.builder()
                .driverId("driver-2")
                .status("CRUISING")
                .latitude(51.5074)
                .longitude(-0.1278)
                .build();

        handler.listen(event.getDriverId(), event);

        assertThat(streamingService.lastEvent.getProviderIdentifier()).isEqualTo("driver-2");
    }

    private static class RecordingStreamingService implements DriverLocationStreamingService {
        private DriverLocationUpdatedEvent lastEvent;

        @Override
        public void streamDriverLocation(DriverLocationUpdatedEvent event) {
            this.lastEvent = event;
        }
    }
}
