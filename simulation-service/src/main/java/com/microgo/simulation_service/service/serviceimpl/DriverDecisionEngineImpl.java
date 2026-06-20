package com.microgo.simulation_service.service.serviceimpl;

import com.microgo.simulation_service.domain.DriverDecision;
import com.microgo.simulation_service.domain.DriverDecisionContext;
import com.microgo.simulation_service.enums.ScenarioType;
import com.microgo.simulation_service.enums.TrafficLevel;
import com.microgo.simulation_service.enums.WeatherCondition;
import com.microgo.simulation_service.enums.ZoneId;
import com.microgo.simulation_service.mapper.DriverDecisionMapper;
import com.microgo.simulation_service.service.DriverDecisionEngine;
import org.springframework.stereotype.Service;

@Service
public class DriverDecisionEngineImpl implements DriverDecisionEngine {

    private static final double BASE_ACCEPTANCE_SCORE = 0.75;
    private static final double ACCEPTANCE_THRESHOLD = 0.50;

    @Override
    public DriverDecision evaluate(DriverDecisionContext context) {
        double pickupDistanceKm = calculatePickupDistanceKm(context);
        double rawAcceptanceScore = calculateRawAcceptanceScore(context, pickupDistanceKm);
        double acceptanceProbability = clampProbability(rawAcceptanceScore);
        boolean accepted = isAccepted(acceptanceProbability);

        return DriverDecisionMapper.toDriverDecision(
                accepted,
                acceptanceProbability,
                determineRefusalReason(context, pickupDistanceKm));
    }

    private double calculatePickupDistanceKm(DriverDecisionContext context) {
        return distanceKm(
                context.getLiveLocation().getLatitude(),
                context.getLiveLocation().getLongitude(),
                context.getPickupLatitude(),
                context.getPickupLongitude());
    }

    private double calculateRawAcceptanceScore(DriverDecisionContext context, double pickupDistanceKm) {
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

    private double pickupDistancePenalty(double pickupDistanceKm) {
        return -Math.min(0.45, pickupDistanceKm / 15.0);
    }

    private double fatigueAdjustment(DriverDecisionContext context) {
        return -(context.getDriver().getFatigueScore() * 0.25);
    }

    private double reliabilityAdjustment(DriverDecisionContext context) {
        return context.getDriver().getReliabilityScore() * 0.20;
    }

    private double fareAdjustment(DriverDecisionContext context) {
        return Math.min(0.20, context.getExpectedFare() / 250.0);
    }

    private double destinationPreferenceAdjustment(DriverDecisionContext context) {
        return context.getDriver().getDestinationBias() == context.getDestinationZone() ? 0.10 : 0.0;
    }

    private double airportRushAdjustment(DriverDecisionContext context) {
        if (isOutsideAirportRush(context)) {
            return 0.0;
        }
        return context.getDriver().getAirportPreference() * 0.20;
    }

    private static boolean isOutsideAirportRush(DriverDecisionContext context) {
        return context.getScenarioContext().getScenario() != ScenarioType.AIRPORT_RUSH;
    }

    private double concertRainAdjustment(DriverDecisionContext context) {
        if (isIsConcertRain(context) && isIsWembleyPickup(context)
                && isDriverOutsideWembley(context)) {
            return -0.18;
        }
        return 0.0;
    }

    private static boolean isDriverOutsideWembley(DriverDecisionContext context) {
        return context.getLiveLocation().getZone() != ZoneId.WEMBLEY_EVENT_ZONE;
    }

    private static boolean isIsWembleyPickup(DriverDecisionContext context) {
        return context.getPickupZone() == ZoneId.WEMBLEY_EVENT_ZONE;
    }

    private static boolean isIsConcertRain(DriverDecisionContext context) {
        return context.getScenarioContext().getScenario() == ScenarioType.CONCERT_RAIN;
    }

    private double trafficAdjustment(DriverDecisionContext context) {
        return context.getScenarioContext().getTrafficLevel() == TrafficLevel.HIGH ? -0.08 : 0.0;
    }

    private double weatherAdjustment(DriverDecisionContext context) {
        return isRainyWeather(context) ? -0.06 : 0.0;
    }

    private double clampProbability(double rawScore) {
        return Math.max(0.0, Math.min(1.0, rawScore));
    }

    private boolean isAccepted(double acceptanceProbability) {
        return acceptanceProbability >= ACCEPTANCE_THRESHOLD;
    }

    private String determineRefusalReason(DriverDecisionContext context, double pickupDistanceKm) {
        if (isLongPickupDistance(pickupDistanceKm)) {
            return "pickup-too-far";
        }
        if (isDriverHighlyFatigued(context)) {
            return "driver-fatigued";
        }
        if (isRainyWeather(context)) {
            return "rain-traffic-penalty";
        }
        return "low-expected-utility";
    }

    private static boolean isRainyWeather(DriverDecisionContext context) {
        return context.getScenarioContext().getWeather() == WeatherCondition.RAIN;
    }

    private static boolean isDriverHighlyFatigued(DriverDecisionContext context) {
        return context.getDriver().getFatigueScore() > 0.75;
    }

    private static boolean isLongPickupDistance(double pickupDistanceKm) {
        return pickupDistanceKm > 8.0;
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadiusKm = 6371.0;
        double latitudeDeltaRadians = Math.toRadians(lat2 - lat1);
        double longitudeDeltaRadians = Math.toRadians(lon2 - lon1);
        double originLatitudeRadians = Math.toRadians(lat1);
        double destinationLatitudeRadians = Math.toRadians(lat2);

        double haversineTerm = getHaversineTerm(latitudeDeltaRadians, originLatitudeRadians, destinationLatitudeRadians, longitudeDeltaRadians);
        double angularDistance = getAngularDistance(haversineTerm);

        return earthRadiusKm * angularDistance;
    }

    private static double getAngularDistance(double haversineTerm) {
        return 2 * Math.atan2(
                Math.sqrt(haversineTerm),
                Math.sqrt(1 - haversineTerm));
    }

    private static double getHaversineTerm(double latitudeDeltaRadians, double originLatitudeRadians, double destinationLatitudeRadians, double longitudeDeltaRadians) {
        return square(Math.sin(latitudeDeltaRadians / 2))
                + (Math.cos(originLatitudeRadians) * Math.cos(destinationLatitudeRadians)
                * square(Math.sin(longitudeDeltaRadians / 2)));
    }

    private static double square(double value) {
        return value * value;
    }
}
