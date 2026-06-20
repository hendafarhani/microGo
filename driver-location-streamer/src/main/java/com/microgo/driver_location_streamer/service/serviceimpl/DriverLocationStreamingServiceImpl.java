package com.microgo.driver_location_streamer.service.serviceimpl;

import com.microgo.driver_location_streamer.model.DriverLocationUpdatedEvent;
import com.microgo.driver_location_streamer.service.DriverLocationStreamingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DriverLocationStreamingServiceImpl implements DriverLocationStreamingService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void streamDriverLocation(DriverLocationUpdatedEvent event) {
        messagingTemplate.convertAndSend(DRIVER_LOCATIONS_DESTINATION, event);
    }
}
