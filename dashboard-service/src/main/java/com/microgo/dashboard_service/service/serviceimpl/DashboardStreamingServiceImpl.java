package com.microgo.dashboard_service.service.serviceimpl;

import com.microgo.dashboard_service.model.RideDashboardMessage;
import com.microgo.dashboard_service.service.DashboardStreamingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardStreamingServiceImpl implements DashboardStreamingService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void streamRideEvent(RideDashboardMessage message) {
        messagingTemplate.convertAndSend(destinationFor(message), message);
    }

    private String destinationFor(RideDashboardMessage message) {
        return rideRequestsDestinationPrefix() + "/" + message.rideRequestIdentifier();
    }

    private String rideRequestsDestinationPrefix() {
        return RIDE_REQUESTS_DESTINATION_PREFIX;
    }
}
