package com.microgo.simulation_service.service.serviceimpl;

import com.microgo.simulation_service.config.SimulationServiceProperties;
import com.microgo.simulation_service.domain.DriverAgent;
import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.enums.ZoneId;
import com.microgo.simulation_service.mapper.DriverAgentMapper;
import com.microgo.simulation_service.service.DriverPopulationGenerator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
@Service
public class DriverPopulationGeneratorImpl implements DriverPopulationGenerator {

    private static final double MIN_FATIGUE_SCORE = 0.1;
    private static final double FATIGUE_SCORE_RANGE = 0.8;
    private static final double MIN_AIRPORT_PREFERENCE = 0.3;
    private static final double AIRPORT_PREFERENCE_RANGE = 0.7;
    private static final double MIN_RELIABILITY_SCORE = 0.5;
    private static final double RELIABILITY_SCORE_RANGE = 0.5;

    private final SimulationServiceProperties properties;

    public DriverPopulationGeneratorImpl(SimulationServiceProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<DriverAgent> generateDrivers(ScenarioContext context) {
        int driverCount = determineDriverCount();
        Random seededRandom = createSeededRandom(context);
        List<DriverAgent> generatedDrivers = new ArrayList<>(driverCount);
        for (int index = 0; index < driverCount; index++) {
            generatedDrivers.add(createDriverAgent(context, seededRandom, index));
        }
        return generatedDrivers;
    }

    private int determineDriverCount() {
        return Math.max(1, properties.getDriverBatchSize());
    }

    private Random createSeededRandom(ScenarioContext context) {
        return new Random(context.getSimulationRunId().getLeastSignificantBits());
    }

    private DriverAgent createDriverAgent(ScenarioContext context, Random seededRandom, int index) {
        return DriverAgentMapper.toDriverAgent(
                context.getSimulationRunId(),
                buildDriverId(context, index),
                resolveHomeZone(context),
                generateFatigueScore(seededRandom),
                generateAirportPreference(seededRandom),
                resolveDestinationBias(context),
                generateReliabilityScore(seededRandom),
                Instant.now());
    }

    private String buildDriverId(ScenarioContext context, int index) {
        return "driver-" + context.getSimulationRunId().toString().substring(0, 8) + "-" + index;
    }

    private ZoneId resolveHomeZone(ScenarioContext context) {
        return isAirportRush(context) ? ZoneId.CENTRAL_LONDON : ZoneId.GENERAL_LONDON;
    }

    private ZoneId resolveDestinationBias(ScenarioContext context) {
        return isAirportRush(context) ? ZoneId.HEATHROW_CORRIDOR : ZoneId.WEMBLEY_EVENT_ZONE;
    }

    private boolean isAirportRush(ScenarioContext context) {
        return context.getScenario().name().equals("AIRPORT_RUSH");
    }

    private double generateFatigueScore(Random seededRandom) {
        return MIN_FATIGUE_SCORE + (seededRandom.nextDouble() * FATIGUE_SCORE_RANGE);
    }

    private double generateAirportPreference(Random seededRandom) {
        return MIN_AIRPORT_PREFERENCE + (seededRandom.nextDouble() * AIRPORT_PREFERENCE_RANGE);
    }

    // Use a deterministic seeded random so the same run id produces reproducible driver behavior.
    private double generateReliabilityScore(Random seededRandom) {
        return MIN_RELIABILITY_SCORE + (seededRandom.nextDouble() * RELIABILITY_SCORE_RANGE);
    }
}
