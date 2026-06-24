package com.microgo.driver_location_streamer.service;

import com.microgo.driver_location_streamer.model.DriverLocationUpdatedEvent;

public interface DriverLocationStreamingService {

    String DRIVER_LOCATIONS_DESTINATION = "/topic/driverLocations";

    void streamDriverLocation(DriverLocationUpdatedEvent event);
}
