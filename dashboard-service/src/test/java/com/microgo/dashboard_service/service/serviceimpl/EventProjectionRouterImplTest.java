package com.microgo.dashboard_service.service.serviceimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microgo.dashboard_service.entity.EventOutboxEntity;
import com.microgo.dashboard_service.entity.RideRequestEntity;
import com.microgo.dashboard_service.enums.RideRequestEventType;
import com.microgo.dashboard_service.mapper.DashboardProjectionMapper;
import com.microgo.dashboard_service.model.DashboardProjection;
import com.microgo.dashboard_service.repository.RideRequestDriverOfferProjection;
import com.microgo.dashboard_service.repository.RideRequestDriverOfferRepository;
import com.microgo.dashboard_service.repository.RideRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventProjectionRouterImplTest {

    private RideRequestRepository rideRequestRepository;
    private RideRequestDriverOfferRepository rideRequestDriverOfferRepository;
    private AtomicReference<RideRequestEntity> rideRequestRef;
    private AtomicReference<RideRequestDriverOfferProjection> rideRequestOfferRef;
    private DashboardProjectionMapper dashboardProjectionMapper;
    private EventProjectionRouterImpl eventProjectionRouter;

    @BeforeEach
    void setUp() {
        rideRequestRef = new AtomicReference<>();
        rideRequestOfferRef = new AtomicReference<>();

        rideRequestRepository = (RideRequestRepository) Proxy.newProxyInstance(
                RideRequestRepository.class.getClassLoader(),
                new Class[]{RideRequestRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findById" -> Optional.ofNullable(rideRequestRef.get());
                    case "save" -> {
                        rideRequestRef.set((RideRequestEntity) args[0]);
                        yield args[0];
                    }
                    default -> unsupported(method.getName());
                }
        );
        rideRequestDriverOfferRepository = (RideRequestDriverOfferRepository) Proxy.newProxyInstance(
                RideRequestDriverOfferRepository.class.getClassLoader(),
                new Class[]{RideRequestDriverOfferRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findProjectionByRideRequestIdAndRiderIdentifier" -> Optional.ofNullable(rideRequestOfferRef.get());
                    default -> unsupported(method.getName());
                }
        );
        dashboardProjectionMapper = new DashboardProjectionMapper(new ObjectMapper());
        eventProjectionRouter = new EventProjectionRouterImpl(
                rideRequestRepository,
                rideRequestDriverOfferRepository,
                dashboardProjectionMapper
        );
    }

    @Test
    void routesRideRequestEventsToRideRequestProjection() {
        RideRequestEntity rideRequest = new RideRequestEntity();
        rideRequest.setId(21L);
        rideRequest.setIdentifier("ride-21");
        rideRequest.setStatus("ACCEPTED");
        rideRequest.setAcceptedRiderIdentifier("rider-21");
        rideRequest.setAcceptedAt(OffsetDateTime.parse("2026-06-18T10:00:00Z"));

        rideRequestRef.set(rideRequest);

        DashboardProjection projection = eventProjectionRouter.route(
                RideRequestEventType.REQUEST_ACCEPTED,
                outboxEvent(21L, "ride-21", "rider-21")
        );

        assertThat(projection.sourceTable()).isEqualTo("RIDE_REQUEST");
        assertThat(projection.data().get("identifier").asText()).isEqualTo("ride-21");
        assertThat(projection.data().get("status").asText()).isEqualTo("ACCEPTED");
        assertThat(projection.data().get("acceptedRiderIdentifier").asText()).isEqualTo("rider-21");
    }

    @Test
    void routesRiderEventsToOfferProjection() {
        RideRequestDriverOfferProjection projection = new RideRequestDriverOfferProjection() {
            @Override
            public Long getId() {
                return 31L;
            }

            @Override
            public Long getRideRequestId() {
                return 21L;
            }

            @Override
            public Integer getNotificationRound() {
                return 2;
            }

            @Override
            public OffsetDateTime getNotifiedAt() {
                return OffsetDateTime.parse("2026-06-18T10:00:00Z");
            }

            @Override
            public String getStatus() {
                return "DECLINED";
            }

            @Override
            public OffsetDateTime getRespondedAt() {
                return OffsetDateTime.parse("2026-06-18T10:02:00Z");
            }

            @Override
            public String getRiderIdentifier() {
                return "rider-21";
            }
        };

        rideRequestOfferRef.set(projection);

        DashboardProjection result = eventProjectionRouter.route(
                RideRequestEventType.RIDER_DECLINED,
                outboxEvent(21L, "ride-21", "rider-21")
        );

        assertThat(result.sourceTable()).isEqualTo("RIDE_REQUEST_DRIVER_OFFER");
        assertThat(result.data().get("riderIdentifier").asText()).isEqualTo("rider-21");
        assertThat(result.data().get("status").asText()).isEqualTo("DECLINED");
        assertThat(result.data().get("notificationRound").asInt()).isEqualTo(2);
    }

    @Test
    void rejectsRiderEventsWithoutRiderIdentifier() {
        assertThatThrownBy(() -> eventProjectionRouter.route(
                RideRequestEventType.RIDER_NOTIFIED,
                outboxEvent(21L, "ride-21", null)
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("missing rider identifier");
    }

    private EventOutboxEntity outboxEvent(Long rideRequestId, String rideRequestIdentifier, String riderId) {
        EventOutboxEntity event = new EventOutboxEntity();
        event.setId(99L);
        event.setEventType("REQUEST_ACCEPTED");
        event.setRideRequestId(rideRequestId);
        event.setRideRequestIdentifier(rideRequestIdentifier);
        event.setRequesterId("user-21");
        event.setRiderId(riderId);
        event.setPayload("{\"rideStatus\":\"PENDING\"}");
        return event;
    }
    private static Object unsupported(String methodName) {
        throw new UnsupportedOperationException("Unexpected repository method call " + methodName);
    }
}
