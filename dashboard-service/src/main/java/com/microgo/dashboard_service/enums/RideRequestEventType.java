package com.microgo.dashboard_service.enums;

public enum RideRequestEventType {

    REQUEST_CREATED,
    REQUEST_STATUS_PENDING,
    REQUEST_TIMED_OUT,
    RIDER_NOTIFIED,
    RIDER_DECLINED,
    REQUEST_ACCEPTED,
    RIDER_CANCELED;

    public boolean isRideRequestEvent() {
        return switch (this) {
            case REQUEST_CREATED, REQUEST_STATUS_PENDING, REQUEST_TIMED_OUT, REQUEST_ACCEPTED -> true;
            case RIDER_NOTIFIED, RIDER_DECLINED, RIDER_CANCELED -> false;
        };
    }
}
