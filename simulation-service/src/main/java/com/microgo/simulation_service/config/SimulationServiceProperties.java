package com.microgo.simulation_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "simulation-service")
public class SimulationServiceProperties {

    private int passengerBatchSize = 25;
    private int driverBatchSize = 15;
    private int maxNearestDrivers = 5;
    private double defaultDriverSpeedKph = 28.0;
    private double defaultRainSpeedFactor = 0.72;
    private double defaultTrafficFactor = 0.68;
    private Topics topics = new Topics();

    @Getter
    @Setter
    public static class Topics {
        private String scenarioStarted;
        private String driverGenerated;
        private String passengerGenerated;
        private String simulatedRideRequested;
        private String driverAccepted;
        private String driverRefused;
        private String simulationMetricsUpdated;
        private String simulationCompleted;
        private String driverLocationUpdated;
        private String driverNotified;
        private String rideAssigned;
        private String rideCancelled;
        private String driverReachedPickup;
        private String driverReachedDestination;
    }
}
