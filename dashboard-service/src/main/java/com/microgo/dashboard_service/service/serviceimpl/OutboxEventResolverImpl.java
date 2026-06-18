package com.microgo.dashboard_service.service.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microgo.dashboard_service.entity.EventOutboxEntity;
import com.microgo.dashboard_service.enums.RideRequestEventType;
import com.microgo.dashboard_service.model.OutboxEventEnvelope;
import com.microgo.dashboard_service.model.ResolvedDashboardEvent;
import com.microgo.dashboard_service.repository.EventOutboxRepository;
import com.microgo.dashboard_service.service.OutboxEventResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventResolverImpl implements OutboxEventResolver {

    private final ObjectMapper objectMapper;
    private final EventOutboxRepository eventOutboxRepository;

    @Override
    public Optional<ResolvedDashboardEvent> resolve(String message) {
        if (messageIsBlank(message)) {
            log.warn("Ignoring blank dashboard event message");
            return Optional.empty();
        }

        try {
            OutboxEventEnvelope envelope = objectMapper.readValue(message, OutboxEventEnvelope.class);
            EventOutboxEntity outboxEvent = findMatchingOutboxEvent(envelope);
            if (outboxEventWasNotFound(outboxEvent)) {
                return Optional.empty();
            }

            RideRequestEventType eventType = parseEventType(outboxEvent);
            if (eventTypeWasNotResolved(eventType)) {
                return Optional.empty();
            }

            JsonNode payload = objectMapper.readTree(outboxEvent.getPayload());
            return Optional.of(new ResolvedDashboardEvent(outboxEvent, eventType, payload));
        } catch (Exception ex) {
            log.warn("Unable to resolve dashboard event {}", message, ex);
            return Optional.empty();
        }
    }

    private EventOutboxEntity findMatchingOutboxEvent(OutboxEventEnvelope envelope) {
        if (eventIdIsMissing(envelope)) {
            log.warn("Ignoring dashboard event without eventId {}", envelope);
            return null;
        }

        EventOutboxEntity outboxEvent = eventOutboxRepository.findById(envelope.eventId()).orElse(null);
        if (outboxEventWasNotFound(outboxEvent)) {
            log.warn("Ignoring dashboard event {} because outbox row was not found", envelope.eventId());
            return null;
        }

        if (eventTypesDoNotMatch(outboxEvent, envelope)) {
            log.warn("Ignoring dashboard event {} because kafka event type {} does not match stored outbox type {}",
                    envelope.eventId(), envelope.eventType(), outboxEvent.getEventType());
            return null;
        }

        return outboxEvent;
    }

    private RideRequestEventType parseEventType(EventOutboxEntity outboxEvent) {
        try {
            return RideRequestEventType.valueOf(outboxEvent.getEventType());
        } catch (IllegalArgumentException ex) {
            log.warn("Ignoring dashboard event {} with unsupported type {}", outboxEvent.getId(), outboxEvent.getEventType());
            return null;
        }
    }

    private boolean messageIsBlank(String message) {
        return !StringUtils.hasText(message);
    }

    private boolean outboxEventWasNotFound(EventOutboxEntity outboxEvent) {
        return outboxEvent == null;
    }

    private boolean eventTypeWasNotResolved(RideRequestEventType eventType) {
        return eventType == null;
    }

    private boolean eventIdIsMissing(OutboxEventEnvelope envelope) {
        return envelope.eventId() == null;
    }

    private boolean eventTypesDoNotMatch(EventOutboxEntity outboxEvent, OutboxEventEnvelope envelope) {
        return !Objects.equals(outboxEvent.getEventType(), envelope.eventType());
    }
}
