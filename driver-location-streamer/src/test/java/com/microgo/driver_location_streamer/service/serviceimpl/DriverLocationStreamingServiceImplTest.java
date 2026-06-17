package com.microgo.driver_location_streamer.service.serviceimpl;

import com.microgo.driver_location_streamer.model.Location;
import com.microgo.driver_location_streamer.model.RiderData;
import com.microgo.driver_location_streamer.service.DriverLocationStreamingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DriverLocationStreamingServiceImplTest {

    private SimpMessagingTemplate messagingTemplate;
    private DriverLocationStreamingServiceImpl service;

    @BeforeEach
    void setUp() {
        messagingTemplate = mock(SimpMessagingTemplate.class);
        service = new DriverLocationStreamingServiceImpl(messagingTemplate);
    }

    @Test
    void streamDriverLocationBroadcastsFullRiderDataPayload() {
        RiderData riderData = RiderData.builder()
                .identifier("rider-1")
                .userName("Ada")
                .location(Location.builder().latitude(48.8584).longitude(2.2945).radius(12).build())
                .build();

        service.streamDriverLocation(riderData);

        verify(messagingTemplate).convertAndSend(
                DriverLocationStreamingService.DRIVER_LOCATIONS_DESTINATION,
                riderData
        );
    }
}
