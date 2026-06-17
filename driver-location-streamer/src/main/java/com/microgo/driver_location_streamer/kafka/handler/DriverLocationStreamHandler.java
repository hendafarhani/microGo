package com.microgo.driver_location_streamer.kafka.handler;

import com.microgo.driver_location_streamer.model.RiderData;
import com.microgo.driver_location_streamer.service.DriverLocationStreamingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DriverLocationStreamHandler {

    private final DriverLocationStreamingService streamingService;

    @KafkaListener(
            id = "${kafka.listeners.driver-location-streamer.id}",
            topics = "${kafka.topics.rider-location}",
            groupId = "${kafka.consumers.driver-location-streamer.group-id}",
            containerFactory = "riderLocationListenerFactory"
    )
    public void listen(@Header(KafkaHeaders.RECEIVED_KEY) String riderIdentifier, RiderData riderData) {
        logReceivedLocation(riderIdentifier, riderData);
        streamingService.streamDriverLocation(riderData);
    }

    private void logReceivedLocation(String riderIdentifier, RiderData riderData) {
        log.info("Streaming rider location from Kafka key {} and payload identifier {}",
                riderIdentifier, riderData.getIdentifier());
    }
}
