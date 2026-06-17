package com.microgo.driver_location_streamer.kafka;

import com.microgo.driver_location_streamer.kafka.handler.DriverLocationStreamHandler;
import com.microgo.driver_location_streamer.model.Location;
import com.microgo.driver_location_streamer.model.RiderData;
import com.microgo.driver_location_streamer.service.DriverLocationStreamingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DriverLocationStreamHandlerTest {

    private DriverLocationStreamingService streamingService;
    private DriverLocationStreamHandler handler;

    @BeforeEach
    void setUp() {
        streamingService = mock(DriverLocationStreamingService.class);
        handler = new DriverLocationStreamHandler(streamingService);
    }

    @Test
    void listenBroadcastsFullRiderDataPayload() {
        RiderData riderData = RiderData.builder()
                .identifier("rider-1")
                .userName("Ada")
                .location(Location.builder().latitude(48.8584).longitude(2.2945).radius(12).build())
                .build();

        handler.listen(riderData.getIdentifier(), riderData);

        verify(streamingService).streamDriverLocation(riderData);
    }
}
