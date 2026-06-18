package com.microgo.dashboard_service.repository;

import java.time.OffsetDateTime;

public interface RideRequestDriverOfferProjection {

    Long getId();

    Long getRideRequestId();

    Integer getNotificationRound();

    OffsetDateTime getNotifiedAt();

    String getStatus();

    OffsetDateTime getRespondedAt();

    String getRiderIdentifier();
}
