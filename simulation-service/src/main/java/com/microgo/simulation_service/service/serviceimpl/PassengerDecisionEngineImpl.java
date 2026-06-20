package com.microgo.simulation_service.service.serviceimpl;

import com.microgo.simulation_service.domain.PassengerAgent;
import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.enums.TrafficLevel;
import com.microgo.simulation_service.enums.WeatherCondition;
import com.microgo.simulation_service.service.PassengerDecisionEngine;
import org.springframework.stereotype.Service;

@Service
public class PassengerDecisionEngineImpl implements PassengerDecisionEngine {

    private static final double BASE_CANCELLATION_RISK = 0.08;
    private static final double CANCELLATION_SENSITIVITY_WEIGHT = 0.35;
    private static final double RAIN_RISK_INCREASE = 0.15;
    private static final double HIGH_TRAFFIC_RISK_INCREASE = 0.12;
    private static final double LONG_WAIT_RISK_INCREASE = 0.10;
    private static final double AIRPORT_HIGH_URGENCY_RISK_INCREASE = 0.06;
    private static final double AIRPORT_BASE_URGENCY_RISK_INCREASE = 0.02;
    private static final double LONG_WAIT_THRESHOLD_SECONDS = 300.0;

    @Override
    public double calculateCancellationRisk(ScenarioContext context, PassengerAgent passenger, double waitingTimeSeconds) {
        double rawCancellationRisk = baseScenarioRisk(context)
                + passengerSensitivityAdjustment(passenger)
                + weatherAdjustment(context)
                + trafficAdjustment(context)
                + waitingTimeAdjustment(waitingTimeSeconds)
                + airportUrgencyAdjustment(context, passenger);
        return clampProbability(rawCancellationRisk);
    }

    private double baseScenarioRisk(ScenarioContext context) {
        return BASE_CANCELLATION_RISK * context.getCancellationRiskMultiplier();
    }

    private double passengerSensitivityAdjustment(PassengerAgent passenger) {
        return passenger.getCancellationSensitivity() * CANCELLATION_SENSITIVITY_WEIGHT;
    }

    private double weatherAdjustment(ScenarioContext context) {
        return context.getWeather() == WeatherCondition.RAIN ? RAIN_RISK_INCREASE : 0.0;
    }

    private double trafficAdjustment(ScenarioContext context) {
        return context.getTrafficLevel() == TrafficLevel.HIGH ? HIGH_TRAFFIC_RISK_INCREASE : 0.0;
    }

    private double waitingTimeAdjustment(double waitingTimeSeconds) {
        return waitingTimeSeconds > LONG_WAIT_THRESHOLD_SECONDS ? LONG_WAIT_RISK_INCREASE : 0.0;
    }

    // Airport passengers are modeled as more cancellation-prone because tight flight schedules
    // make them less tolerant of delay even when they strongly need the trip.
    private double airportUrgencyAdjustment(ScenarioContext context, PassengerAgent passenger) {
        if (!context.getScenario().name().equals("AIRPORT_RUSH")) {
            return 0.0;
        }
        return passenger.getUrgencyScore() > 0.8
                ? AIRPORT_HIGH_URGENCY_RISK_INCREASE
                : AIRPORT_BASE_URGENCY_RISK_INCREASE;
    }

    private double clampProbability(double rawCancellationRisk) {
        return Math.max(0.0, Math.min(1.0, rawCancellationRisk));
    }
}
