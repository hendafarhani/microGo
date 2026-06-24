package com.microgo.optimization_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "optimization-service")
public class OptimizationServiceProperties {

    private int maxNearestDrivers = 5;
    private int minimumCentralLondonSupply = 3;
    private int maxDriversPerTargetZone = 8;
    private int reachabilityThresholdMinutes = 45;
    private int staleDriverThresholdSeconds = 120;
    private double defaultTrafficMultiplier = 1.0;
    private double concertRainTrafficMultiplier = 0.78;
    private double airportRushTrafficMultiplier = 0.84;

    private Redis redis = new Redis();
    private Topics topics = new Topics();
    private Listeners listeners = new Listeners();
    private Consumers consumers = new Consumers();

    @Getter
    @Setter
    public static class Redis {
        private String geoKey = "vehicle_location";
        private String stateKeyPrefix = "driver:geo-state:";
        private String zoneKeyPrefix = "driver:zone:";
    }

    @Getter
    @Setter
    public static class Topics {
        private String scenarioStarted;
        private String simulatedRideRequested;
        private String simulationMetricsUpdated;
        private String simulationCompleted;
        private String driverLocationUpdated;
        private String rideAssigned;
        private String rideCancelled;
        private String optimizationRequested;
        private String optimizationCompleted;
        private String driverRepositioningRecommended;
        private String optimizedAssignmentRecommended;
    }

    @Getter
    @Setter
    public static class Listeners {
        private String scenarioStartedId = "optimizationScenarioStartedListener";
        private String simulatedRideRequestedId = "optimizationRideRequestedListener";
        private String simulationMetricsUpdatedId = "optimizationMetricsListener";
        private String simulationCompletedId = "optimizationCompletedListener";
        private String driverLocationUpdatedId = "optimizationDriverLocationListener";
        private String rideAssignedId = "optimizationRideAssignedListener";
        private String rideCancelledId = "optimizationRideCancelledListener";
    }

    @Getter
    @Setter
    public static class Consumers {
        private String scenarioStartedGroupId = "optimization.service.scenario-started.group";
        private String simulatedRideRequestedGroupId = "optimization.service.ride-requested.group";
        private String simulationMetricsUpdatedGroupId = "optimization.service.metrics.group";
        private String simulationCompletedGroupId = "optimization.service.completed.group";
        private String driverLocationUpdatedGroupId = "optimization.service.driver-location.group";
        private String rideAssignedGroupId = "optimization.service.ride-assigned.group";
        private String rideCancelledGroupId = "optimization.service.ride-cancelled.group";
    }
}
