package com.microgo.dashboard_service.service.serviceimpl;

import com.microgo.dashboard_service.mapper.DashboardProjectionMapper;
import com.microgo.dashboard_service.entity.EventOutboxEntity;
import com.microgo.dashboard_service.entity.RideRequestEntity;
import com.microgo.dashboard_service.enums.RideRequestEventType;
import com.microgo.dashboard_service.model.DashboardProjection;
import com.microgo.dashboard_service.repository.RideRequestDriverOfferProjection;
import com.microgo.dashboard_service.repository.RideRequestDriverOfferRepository;
import com.microgo.dashboard_service.repository.RideRequestRepository;
import com.microgo.dashboard_service.service.EventProjectionRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EventProjectionRouterImpl implements EventProjectionRouter {

    private final RideRequestRepository rideRequestRepository;
    private final RideRequestDriverOfferRepository rideRequestDriverOfferRepository;
    private final DashboardProjectionMapper dashboardProjectionMapper;

    @Override
    public DashboardProjection route(RideRequestEventType eventType, EventOutboxEntity outboxEvent) {
        if (isRideRequestEvent(eventType)) {
            return projectRideRequest(outboxEvent);
        }
        return projectRideRequestOffer(outboxEvent);
    }

    private DashboardProjection projectRideRequest(EventOutboxEntity outboxEvent) {
        RideRequestEntity rideRequest = rideRequestRepository.findById(outboxEvent.getRideRequestId())
                .orElseThrow(() -> new IllegalStateException(
                        "Ride request not found for outbox event " + outboxEvent.getId()));
        return dashboardProjectionMapper.mapRideRequestProjection(rideRequest);
    }

    private DashboardProjection projectRideRequestOffer(EventOutboxEntity outboxEvent) {
        RideRequestDriverOfferProjection offer = rideRequestDriverOfferRepository
                .findProjectionByRideRequestIdAndRiderIdentifier(outboxEvent.getRideRequestId(), requiredRiderIdentifier(outboxEvent))
                .orElseThrow(() -> new IllegalStateException(
                        "Ride request offer not found for outbox event " + outboxEvent.getId()));
        return dashboardProjectionMapper.mapRideRequestOfferProjection(offer);
    }

    private String requiredRiderIdentifier(EventOutboxEntity outboxEvent) {
        if (riderIdentifierIsPresent(outboxEvent)) {
            return outboxEvent.getRiderId();
        }
        throw new IllegalStateException("Rider event " + outboxEvent.getId() + " is missing rider identifier");
    }

    private boolean isRideRequestEvent(RideRequestEventType eventType) {
        return eventType.isRideRequestEvent();
    }

    private boolean riderIdentifierIsPresent(EventOutboxEntity outboxEvent) {
        return StringUtils.hasText(outboxEvent.getRiderId());
    }
}
