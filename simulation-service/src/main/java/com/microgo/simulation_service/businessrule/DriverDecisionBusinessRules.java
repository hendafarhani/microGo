package com.microgo.simulation_service.businessrule;

import com.microgo.simulation_service.domain.DriverDecisionContext;
import com.microgo.simulation_service.enums.ScenarioType;
import com.microgo.simulation_service.enums.TrafficLevel;
import com.microgo.simulation_service.enums.WeatherCondition;
import com.microgo.simulation_service.enums.ZoneId;

public final class DriverDecisionBusinessRules {

    private static final double BASE_ACCEPTANCE_SCORE = 0.75;
    private static final double ACCEPTANCE_THRESHOLD = 0.50;
    private static final double MAX_PICKUP_DISTANCE_PENALTY = 0.45;
    private static final double PICKUP_DISTANCE_PENALTY_DIVISOR = 15.0;
    private static final double FATIGUE_WEIGHT = 0.25;
    private static final double RELIABILITY_WEIGHT = 0.20;
    private static final double MAX_FARE_BONUS = 0.20;
    private static final double FARE_BONUS_DIVISOR = 250.0;
    private static final double DESTINATION_PREFERENCE_BONUS = 0.10;
    private static final double AIRPORT_PREFERENCE_WEIGHT = 0.20;
    private static final double CONCERT_RAIN_WEMBLEY_PENALTY = 0.18;
    private static final double HIGH_TRAFFIC_PENALTY = 0.08;
    private static final double RAIN_PENALTY = 0.06;
    private static final double LONG_PICKUP_DISTANCE_KM = 8.0;
    private static final double HIGH_FATIGUE_THRESHOLD = 0.75;
    private static final double EARTH_RADIUS_KM = 6371.0;

    private DriverDecisionBusinessRules() {
    }

    public static DriverDecisionRuleOutcome evaluate(DriverDecisionContext context) {
        double pickupDistanceKm = calculatePickupDistanceKm(context);
        double rawAcceptanceScore = calculateRawAcceptanceScore(context, pickupDistanceKm);
        double acceptanceProbability = clampProbability(rawAcceptanceScore);
        boolean accepted = acceptanceProbability >= ACCEPTANCE_THRESHOLD;

        return new DriverDecisionRuleOutcome(
                accepted,
                acceptanceProbability,
                determineRefusalReason(context, pickupDistanceKm));
    }

    private static double calculatePickupDistanceKm(DriverDecisionContext context) {
        return distanceKm(
                context.getLiveLocation().getLatitude(),
                context.getLiveLocation().getLongitude(),
                context.getPickupLatitude(),
                context.getPickupLongitude());
    }

    private static double calculateRawAcceptanceScore(DriverDecisionContext context, double pickupDistanceKm) {
        return BASE_ACCEPTANCE_SCORE
                + pickupDistancePenalty(pickupDistanceKm)
                + fatigueAdjustment(context)
                + reliabilityAdjustment(context)
                + fareAdjustment(context)
                + destinationPreferenceAdjustment(context)
                + airportRushAdjustment(context)
                + concertRainAdjustment(context)
                + trafficAdjustment(context)
                + weatherAdjustment(context);
    }

    private static double pickupDistancePenalty(double pickupDistanceKm) {
        return -Math.min(MAX_PICKUP_DISTANCE_PENALTY, pickupDistanceKm / PICKUP_DISTANCE_PENALTY_DIVISOR);
    }

    private static double fatigueAdjustment(DriverDecisionContext context) {
        return -(context.getDriver().getFatigueScore() * FATIGUE_WEIGHT);
    }

    private static double reliabilityAdjustment(DriverDecisionContext context) {
        return context.getDriver().getReliabilityScore() * RELIABILITY_WEIGHT;
    }

    private static double fareAdjustment(DriverDecisionContext context) {
        return Math.min(MAX_FARE_BONUS, context.getExpectedFare() / FARE_BONUS_DIVISOR);
    }

    private static double destinationPreferenceAdjustment(DriverDecisionContext context) {
        return context.getDriver().getDestinationBias() == context.getDestinationZone()
                ? DESTINATION_PREFERENCE_BONUS
                : 0.0;
    }

    private static double airportRushAdjustment(DriverDecisionContext context) {
        if (context.getScenarioContext().getScenario() != ScenarioType.AIRPORT_RUSH) {
            return 0.0;
        }
        return context.getDriver().getAirportPreference() * AIRPORT_PREFERENCE_WEIGHT;
    }

    private static double concertRainAdjustment(DriverDecisionContext context) {
        if (context.getScenarioContext().getScenario() == ScenarioType.CONCERT_RAIN
                && context.getPickupZone() == ZoneId.WEMBLEY_EVENT_ZONE
                && context.getLiveLocation().getZone() != ZoneId.WEMBLEY_EVENT_ZONE) {
            return -CONCERT_RAIN_WEMBLEY_PENALTY;
        }
        return 0.0;
    }

    private static double trafficAdjustment(DriverDecisionContext context) {
        return context.getScenarioContext().getTrafficLevel() == TrafficLevel.HIGH
                ? -HIGH_TRAFFIC_PENALTY
                : 0.0;
    }

    private static double weatherAdjustment(DriverDecisionContext context) {
        return context.getScenarioContext().getWeather() == WeatherCondition.RAIN
                ? -RAIN_PENALTY
                : 0.0;
    }

    private static double clampProbability(double rawScore) {
        return Math.max(0.0, Math.min(1.0, rawScore));
    }

    private static String determineRefusalReason(DriverDecisionContext context, double pickupDistanceKm) {
        if (pickupDistanceKm > LONG_PICKUP_DISTANCE_KM) {
            return "pickup-too-far";
        }
        if (context.getDriver().getFatigueScore() > HIGH_FATIGUE_THRESHOLD) {
            return "driver-fatigued";
        }
        if (context.getScenarioContext().getWeather() == WeatherCondition.RAIN) {
            return "rain-traffic-penalty";
        }
        return "low-expected-utility";
    }

    private static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double latitudeDeltaRadians = Math.toRadians(lat2 - lat1);
        double longitudeDeltaRadians = Math.toRadians(lon2 - lon1);
        double originLatitudeRadians = Math.toRadians(lat1);
        double destinationLatitudeRadians = Math.toRadians(lat2);

        double haversineTerm = square(Math.sin(latitudeDeltaRadians / 2))
                + (Math.cos(originLatitudeRadians) * Math.cos(destinationLatitudeRadians)
                * square(Math.sin(longitudeDeltaRadians / 2)));
        double angularDistance = 2 * Math.atan2(
                Math.sqrt(haversineTerm),
                Math.sqrt(1 - haversineTerm));

        return EARTH_RADIUS_KM * angularDistance;
    }

    private static double square(double value) {
        return value * value;
    }

    public record DriverDecisionRuleOutcome(
            boolean accepted,
            double acceptanceProbability,
            String refusalReason) {
    }
}

