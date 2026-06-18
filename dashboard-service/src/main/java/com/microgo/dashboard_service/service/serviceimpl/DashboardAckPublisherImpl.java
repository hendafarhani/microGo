package com.microgo.dashboard_service.service.serviceimpl;

import com.microgo.dashboard_service.configuration.DashboardServiceProperties;
import com.microgo.dashboard_service.mapper.DashboardAckMessageMapper;
import com.microgo.dashboard_service.model.DashboardAckMessage;
import com.microgo.dashboard_service.service.DashboardAckPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DashboardAckPublisherImpl implements DashboardAckPublisher {

    private static final String WEBSOCKET_PUBLISHED = "WEBSOCKET_PUBLISHED";
    private static final int SEND_TIMEOUT_SECONDS = 10;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DashboardServiceProperties properties;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final DashboardAckMessageMapper dashboardAckMessageMapper;

    @Value("${spring.application.name}")
    private String serviceName;

    @Override
    public void publishWebsocketPublished(Long eventId) {
        try {
            sendAcknowledgement(eventId, dashboardAckMessageMapper.mapWebsocketPublished(eventId, serviceName));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to publish dashboard acknowledgement for event " + eventId, ex);
        }
    }

    private void sendAcknowledgement(Long eventId, DashboardAckMessage ackMessage) throws Exception {
        kafkaTemplate.send(ackTopic(), eventKey(eventId), serialize(ackMessage))
                .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private String ackTopic() {
        return properties.ackTopic();
    }

    private String eventKey(Long eventId) {
        return String.valueOf(eventId);
    }

    private String serialize(DashboardAckMessage ackMessage) throws com.fasterxml.jackson.core.JsonProcessingException {
        return objectMapper.writeValueAsString(ackMessage);
    }
}
