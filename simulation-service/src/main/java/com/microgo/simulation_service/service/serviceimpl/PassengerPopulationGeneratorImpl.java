package com.microgo.simulation_service.service.serviceimpl;

import com.microgo.simulation_service.config.SimulationServiceProperties;
import com.microgo.simulation_service.domain.PassengerAgent;
import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.enums.ZoneId;
import com.microgo.simulation_service.mapper.PassengerAgentMapper;
import com.microgo.simulation_service.service.PassengerPopulationGenerator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class PassengerPopulationGeneratorImpl implements PassengerPopulationGenerator {

    private static final double MIN_URGENCY_SCORE = 0.45;
    private static final double URGENCY_SCORE_RANGE = 0.5;
    private static final double MIN_CANCELLATION_SENSITIVITY = 0.25;
    private static final double CANCELLATION_SENSITIVITY_RANGE = 0.4;

    private final SimulationServiceProperties properties;

    public PassengerPopulationGeneratorImpl(SimulationServiceProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<PassengerAgent> generatePassengers(ScenarioContext context) {
        int passengerCount = determinePassengerCount(context);
        Random seededRandom = createSeededRandom(context);
        List<PassengerAgent> generatedPassengers = new ArrayList<>(passengerCount);
        for (int index = 0; index < passengerCount; index++) {
            generatedPassengers.add(createPassengerAgent(context, seededRandom, index));
        }
        return generatedPassengers;
    }

    private int determinePassengerCount(ScenarioContext context) {
        return Math.max(1, (int) Math.ceil(properties.getPassengerBatchSize() * context.getPassengerDemandMultiplier()));
    }

    private Random createSeededRandom(ScenarioContext context) {
        return new Random(context.getSimulationRunId().getMostSignificantBits());
    }

    private PassengerAgent createPassengerAgent(ScenarioContext context, Random seededRandom, int index) {
        ZoneId originZone = context.getAnchorZone();
        ZoneId destinationZone = resolveDestinationZone(context);
        return PassengerAgentMapper.toPassengerAgent(
                context.getSimulationRunId(),
                buildPassengerId(context, index),
                originZone,
                destinationZone,
                generateUrgencyScore(seededRandom),
                generateCancellationSensitivity(seededRandom),
                generateLatitude(originZone, seededRandom),
                generateLongitude(originZone, seededRandom),
                generateLatitude(destinationZone, seededRandom),
                generateLongitude(destinationZone, seededRandom),
                Instant.now());
    }

    private String buildPassengerId(ScenarioContext context, int index) {
        return "passenger-" + context.getSimulationRunId().toString().substring(0, 8) + "-" + index;
    }

    private ZoneId resolveDestinationZone(ScenarioContext context) {
        return context.getScenario().name().equals("AIRPORT_RUSH")
                ? ZoneId.HEATHROW_CORRIDOR
                : ZoneId.CENTRAL_LONDON;
    }

    private double generateUrgencyScore(Random seededRandom) {
        return MIN_URGENCY_SCORE + (seededRandom.nextDouble() * URGENCY_SCORE_RANGE);
    }

    private double generateCancellationSensitivity(Random seededRandom) {
        return MIN_CANCELLATION_SENSITIVITY + (seededRandom.nextDouble() * CANCELLATION_SENSITIVITY_RANGE);
    }

    // We add a small bounded offset around the zone anchor so generated passengers cluster
    // by scenario while still producing enough variation for matching and forecasting tests.
    private double generateLatitude(ZoneId zone, Random seededRandom) {
        return latitudeForZone(zone) + seededRandom.nextDouble() / 100;
    }

    private double generateLongitude(ZoneId zone, Random seededRandom) {
        return longitudeForZone(zone) + seededRandom.nextDouble() / 100;
    }

    private double latitudeForZone(ZoneId zone) {
        return switch (zone) {
            case WEMBLEY_EVENT_ZONE -> 51.5560;
            case HEATHROW_CORRIDOR -> 51.4700;
            case CENTRAL_LONDON -> 51.5074;
            case GENERAL_LONDON -> 51.5000;
        };
    }

    private double longitudeForZone(ZoneId zone) {
        return switch (zone) {
            case WEMBLEY_EVENT_ZONE -> -0.2796;
            case HEATHROW_CORRIDOR -> -0.4543;
            case CENTRAL_LONDON -> -0.1278;
            case GENERAL_LONDON -> -0.1000;
        };
    }
}
