package com.microgo.dashboard_service.service.serviceimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microgo.dashboard_service.entity.EventOutboxEntity;
import com.microgo.dashboard_service.enums.RideRequestEventType;
import com.microgo.dashboard_service.mapper.RideDashboardMessageMapper;
import com.microgo.dashboard_service.model.DashboardProjection;
import com.microgo.dashboard_service.model.ResolvedDashboardEvent;
import com.microgo.dashboard_service.model.RideDashboardMessage;
import com.microgo.dashboard_service.service.DashboardAckPublisher;
import com.microgo.dashboard_service.service.DashboardStreamingService;
import com.microgo.dashboard_service.service.EventProjectionRouter;
import com.microgo.dashboard_service.service.OutboxEventResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardEventProcessorImplTest {

    private RecordingOutboxEventResolver outboxEventResolver;
    private RecordingEventProjectionRouter eventProjectionRouter;
    private RideDashboardMessageMapper rideDashboardMessageMapper;
    private RecordingDashboardStreamingService dashboardStreamingService;
    private RecordingDashboardAckPublisher dashboardAckPublisher;
    private DashboardEventProcessorImpl dashboardEventProcessor;

    @BeforeEach
    void setUp() {
        outboxEventResolver = new RecordingOutboxEventResolver();
        eventProjectionRouter = new RecordingEventProjectionRouter();
        rideDashboardMessageMapper = new RideDashboardMessageMapper();
        dashboardStreamingService = new RecordingDashboardStreamingService();
        dashboardAckPublisher = new RecordingDashboardAckPublisher();
        dashboardEventProcessor = new DashboardEventProcessorImpl(
                outboxEventResolver,
                eventProjectionRouter,
                rideDashboardMessageMapper,
                dashboardStreamingService,
                dashboardAckPublisher
        );
    }

    @Test
    void streamsProjectionAndPublishesAckWhenOutboxMatches() {
        EventOutboxEntity event = eventOutbox("REQUEST_ACCEPTED");
        ObjectNode data = new ObjectMapper().createObjectNode();
        data.put("status", "ACCEPTED");
        outboxEventResolver.resolvedEvent = new ResolvedDashboardEvent(
                event,
                RideRequestEventType.REQUEST_ACCEPTED,
                new ObjectMapper().createObjectNode().put("rideStatus", "ACCEPTED")
        );
        eventProjectionRouter.projection = new DashboardProjection("RIDE_REQUEST", data);

        boolean processed = dashboardEventProcessor.process("""
                {"eventId":7,"eventType":"REQUEST_ACCEPTED","rideRequestIdentifier":"ride-7","requesterId":"user-7","riderId":"rider-7","rideStatus":"ACCEPTED","payload":{"rideStatus":"ACCEPTED"}}
                """);

        assertThat(processed).isTrue();
        assertThat(dashboardStreamingService.messages).hasSize(1);
        assertThat(dashboardStreamingService.messages.getFirst().sourceTable()).isEqualTo("RIDE_REQUEST");
        assertThat(dashboardAckPublisher.acknowledgedEventIds).containsExactly(7L);
    }

    @Test
    void ignoresEventWhenStoredOutboxTypeDoesNotMatchKafkaType() {
        outboxEventResolver.resolvedEvent = null;

        boolean processed = dashboardEventProcessor.process("""
                {"eventId":7,"eventType":"REQUEST_ACCEPTED","rideRequestIdentifier":"ride-7","requesterId":"user-7","riderId":"rider-7","rideStatus":"ACCEPTED","payload":{"rideStatus":"ACCEPTED"}}
                """);

        assertThat(processed).isFalse();
        assertThat(dashboardStreamingService.messages).isEmpty();
        assertThat(dashboardAckPublisher.acknowledgedEventIds).isEmpty();
    }

    private EventOutboxEntity eventOutbox(String eventType) {
        EventOutboxEntity event = new EventOutboxEntity();
        event.setId(7L);
        event.setEventType(eventType);
        event.setRideRequestId(70L);
        event.setRideRequestIdentifier("ride-7");
        event.setRequesterId("user-7");
        event.setRiderId("rider-7");
        event.setPayload("{\"rideStatus\":\"ACCEPTED\"}");
        return event;
    }

    private static final class RecordingOutboxEventResolver implements OutboxEventResolver {
        private ResolvedDashboardEvent resolvedEvent;

        @Override
        public Optional<ResolvedDashboardEvent> resolve(String message) {
            return Optional.ofNullable(resolvedEvent);
        }
    }

    private static final class RecordingEventProjectionRouter implements EventProjectionRouter {
        private DashboardProjection projection;

        @Override
        public DashboardProjection route(com.microgo.dashboard_service.enums.RideRequestEventType eventType, EventOutboxEntity outboxEvent) {
            return projection;
        }
    }

    private static final class RecordingDashboardStreamingService implements DashboardStreamingService {
        private final List<RideDashboardMessage> messages = new ArrayList<>();

        @Override
        public void streamRideEvent(RideDashboardMessage message) {
            messages.add(message);
        }
    }

    private static final class RecordingDashboardAckPublisher implements DashboardAckPublisher {
        private final List<Long> acknowledgedEventIds = new ArrayList<>();

        @Override
        public void publishWebsocketPublished(Long eventId) {
            acknowledgedEventIds.add(eventId);
        }
    }
}
