package com.microgo.driver_location_streamer.service;

import com.microgo.driver_location_streamer.model.RiderData;

public interface DriverLocationStreamingService {

    String DRIVER_LOCATIONS_DESTINATION = "/topic/driverLocations";

    void streamDriverLocation(RiderData riderData);
}
