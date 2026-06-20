package com.microgo.driver_location_streamer.kafka.handler;

import com.microgo.driver_location_streamer.model.DriverLocationUpdatedEvent;
import com.microgo.driver_location_streamer.service.DriverLocationStreamingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
    public void listen(@Header(KafkaHeaders.RECEIVED_KEY) String driverId, DriverLocationUpdatedEvent event) {
        logReceivedLocation(driverId, event);
        streamingService.streamDriverLocation(withProviderIdentifier(event));
    }

    private void logReceivedLocation(String driverId, DriverLocationUpdatedEvent event) {
        log.info("Streaming driver location from Kafka key {} and payload driverId {}",
                driverId, event.getDriverId());
    }

    private DriverLocationUpdatedEvent withProviderIdentifier(DriverLocationUpdatedEvent event) {
        if (event != null) {
            if (!StringUtils.hasText(event.getDriverIdentifier())) {
                event.setDriverIdentifier(event.getDriverId());
            }
            if (!StringUtils.hasText(event.getProviderIdentifier())) {
                event.setProviderIdentifier(event.getDriverIdentifier());
            }
            if (!StringUtils.hasText(event.getDriverDisplayId()) && StringUtils.hasText(event.getDriverIdentifier())) {
                event.setDriverDisplayId("DRV-" + event.getDriverIdentifier().toUpperCase().replaceAll("[^A-Z0-9]+", "-"));
            }
        }
        return event;
    }
}
