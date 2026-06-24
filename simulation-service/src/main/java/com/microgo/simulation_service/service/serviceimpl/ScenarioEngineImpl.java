package com.microgo.simulation_service.service.serviceimpl;

import com.microgo.simulation_service.domain.OptimizationSnapshot;
import com.microgo.simulation_service.domain.PassengerAgent;
import com.microgo.simulation_service.domain.ScenarioContext;
import com.microgo.simulation_service.enums.ScenarioType;
import com.microgo.simulation_service.domain.SimulationMetricsSnapshot;
import com.microgo.simulation_service.enums.SimulationRunStatus;
import com.microgo.simulation_service.entity.ScenarioConfigEntity;
import com.microgo.simulation_service.entity.SimulationResultEntity;
import com.microgo.simulation_service.entity.SimulationRunEntity;
import com.microgo.simulation_service.kafka.model.ScenarioStartedEvent;
import com.microgo.simulation_service.kafka.model.SimulatedRideRequestedEvent;
import com.microgo.simulation_service.kafka.publisher.impl.RideRequestPublisherImpl;
import com.microgo.simulation_service.mapper.ScenarioConfigMapper;
import com.microgo.simulation_service.mapper.ScenarioContextMapper;
import com.microgo.simulation_service.mapper.SimulationRunMapper;
import com.microgo.simulation_service.repository.ScenarioConfigRepository;
import com.microgo.simulation_service.repository.SimulationResultRepository;
import com.microgo.simulation_service.repository.SimulationRunRepository;
import com.microgo.simulation_service.service.ForecastCalculator;
import com.microgo.simulation_service.service.PassengerDecisionEngine;
import com.microgo.simulation_service.service.PopulationEngine;
import com.microgo.simulation_service.domain.PopulationSnapshot;
import com.microgo.simulation_service.service.RideRequestGenerator;
import com.microgo.simulation_service.service.ScenarioEngine;
import com.microgo.simulation_service.service.SimulationMetricsCollector;
import com.microgo.simulation_service.service.SimulationResultBuilder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
public class ScenarioEngineImpl implements ScenarioEngine {

    private final ScenarioConfigRepository scenarioConfigRepository;
    private final SimulationRunRepository simulationRunRepository;
    private final SimulationResultRepository simulationResultRepository;
    private final PopulationEngine populationEngine;
    private final RideRequestGenerator rideRequestGenerator;
    private final RideRequestPublisherImpl rideRequestPublisherImpl;
    private final PassengerDecisionEngine passengerDecisionEngine;
    private final SimulationMetricsCollector simulationMetricsCollector;
    private final ForecastCalculator forecastCalculator;
    private final SimulationResultBuilder simulationResultBuilder;

    private final ConcurrentMap<UUID, ScenarioContext> activeRuns = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, UUID> driverToRun = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public UUID startSimulation(String scenarioName, String requestedBy) {
        ScenarioType scenarioType = parseScenarioType(scenarioName);
        ScenarioConfigEntity scenarioConfig = loadOrCreateScenarioConfig(scenarioType);
        SimulationRunEntity runEntity = createSimulationRun(scenarioConfig, requestedBy);
        ScenarioContext context = buildScenarioContext(runEntity, scenarioConfig);

        registerActiveScenario(context);
        publishScenarioStartedEvent(runEntity, context);

        PopulationSnapshot population = bootstrapPopulation(context, runEntity);
        List<SimulatedRideRequestedEvent> rideRequests = publishRideRequests(context, population);

        initializeAndPublishMetrics(runEntity, context, population.passengers(), rideRequests.size());
        return runEntity.getId();
    }

    @Override
    @Transactional
    public SimulationResultEntity stopSimulation(UUID simulationRunId) {
        SimulationRunEntity runEntity = getSimulationRun(simulationRunId);
        ScenarioContext context = resolveScenarioContext(simulationRunId, runEntity);
        SimulationMetricsSnapshot metricsSnapshot = simulationMetricsCollector.snapshot(simulationRunId);
        OptimizationSnapshot optimizationSnapshot = forecastCalculator.calculate(context, metricsSnapshot);
        SimulationResultEntity result = persistSimulationResult(runEntity, optimizationSnapshot);

        markRunAsCompleted(runEntity, optimizationSnapshot);
        publishSimulationCompletedEvent(simulationRunId, optimizationSnapshot);
        clearActiveSimulationState(simulationRunId);
        return result;
    }

    @Override
    public SimulationRunEntity getSimulationRun(UUID simulationRunId) {
        return simulationRunRepository.findById(simulationRunId)
                .orElseThrow(() -> new IllegalArgumentException("Simulation run not found: " + simulationRunId));
    }

    @Override
    public Optional<ScenarioContext> findActiveScenarioByDriverId(String driverId) {
        UUID runId = driverToRun.get(driverId);
        return Optional.ofNullable(runId).map(activeRuns::get);
    }

    private ScenarioType parseScenarioType(String scenarioName) {
        return ScenarioType.valueOf(scenarioName.toUpperCase());
    }

    private ScenarioConfigEntity loadOrCreateScenarioConfig(ScenarioType scenarioType) {
        return scenarioConfigRepository.findByScenarioName(scenarioType)
                .orElseGet(() -> scenarioConfigRepository.save(defaultConfigForScenario(scenarioType)));
    }

    private SimulationRunEntity createSimulationRun(ScenarioConfigEntity scenarioConfig, String requestedBy) {
        return simulationRunRepository.save(SimulationRunEntity.builder()
                .id(UUID.randomUUID())
                .scenarioConfig(scenarioConfig)
                .status(SimulationRunStatus.RUNNING)
                .startedAt(Instant.now())
                .requestedBy(requestedBy)
                .notes("Started by SimulationController")
                .build());
    }

    private ScenarioContext buildScenarioContext(SimulationRunEntity runEntity, ScenarioConfigEntity entity) {
        return ScenarioContextMapper.toScenarioContext(runEntity, entity);
    }

    private void registerActiveScenario(ScenarioContext context) {
        activeRuns.put(context.getSimulationRunId(), context);
    }

    private void publishScenarioStartedEvent(SimulationRunEntity runEntity, ScenarioContext context) {
        rideRequestPublisherImpl.publishScenarioStarted(ScenarioStartedEvent.builder()
                .simulationRunId(runEntity.getId())
                .scenario(context.getScenario())
                .startedAt(runEntity.getStartedAt())
                .build());
    }

    private PopulationSnapshot bootstrapPopulation(ScenarioContext context, SimulationRunEntity runEntity) {
        PopulationSnapshot population = populationEngine.bootstrapPopulation(context, runEntity);
        population.drivers().forEach(driver -> driverToRun.put(driver.getDriverId(), runEntity.getId()));
        return population;
    }

    private List<SimulatedRideRequestedEvent> publishRideRequests(
            ScenarioContext context,
            PopulationSnapshot population) {
        List<SimulatedRideRequestedEvent> rideRequests =
                rideRequestGenerator.generateRideRequests(context, population.passengers());
        rideRequests.forEach(rideRequestPublisherImpl::publishRideRequested);
        return rideRequests;
    }

    private void initializeAndPublishMetrics(
            SimulationRunEntity runEntity,
            ScenarioContext context,
            List<PassengerAgent> passengers,
            int pendingRideRequests) {
        // We derive the initial metrics snapshot from generated passengers before any external ride lifecycle
        // feedback arrives, giving optimization a usable baseline immediately after scenario start.
        double cancellationRisk = calculateAverageCancellationRisk(context, passengers);
        SimulationMetricsSnapshot snapshot = simulationMetricsCollector.initializeRun(
                runEntity,
                pendingRideRequests,
                cancellationRisk);
        rideRequestPublisherImpl.publishSimulationMetrics(snapshot, context.getScenario().name());
    }

    private ScenarioContext resolveScenarioContext(UUID simulationRunId, SimulationRunEntity runEntity) {
        return Optional.ofNullable(activeRuns.get(simulationRunId))
                .orElseGet(() -> buildScenarioContext(runEntity, runEntity.getScenarioConfig()));
    }

    private SimulationResultEntity persistSimulationResult(
            SimulationRunEntity runEntity,
            OptimizationSnapshot optimizationSnapshot) {
        SimulationResultEntity result = simulationResultBuilder.build(runEntity, optimizationSnapshot);
        simulationResultRepository.save(result);
        return result;
    }

    private void markRunAsCompleted(SimulationRunEntity runEntity, OptimizationSnapshot optimizationSnapshot) {
        SimulationRunMapper.markRunAsCompleted(runEntity, optimizationSnapshot);
        simulationRunRepository.save(runEntity);
    }

    private void publishSimulationCompletedEvent(UUID simulationRunId, OptimizationSnapshot optimizationSnapshot) {
        rideRequestPublisherImpl.publishSimulationCompleted(optimizationSnapshot, simulationRunId.toString());
    }

    private void clearActiveSimulationState(UUID simulationRunId) {
        activeRuns.remove(simulationRunId);
        driverToRun.entrySet().removeIf(entry -> simulationRunId.equals(entry.getValue()));
    }

    private ScenarioConfigEntity defaultConfigForScenario(ScenarioType scenarioType) {
        Instant now = Instant.now();
        if (isConcertRainScenario(scenarioType)) {
            return ScenarioConfigMapper.defaultConcertRainConfig(now);
        }
        return ScenarioConfigMapper.defaultAirportRushConfig(now);
    }

    private static boolean isConcertRainScenario(ScenarioType scenarioType) {
        return scenarioType == ScenarioType.CONCERT_RAIN;
    }

    // The initial cancellation metric is a coarse scenario baseline, not a live observed rate.
    private double calculateAverageCancellationRisk(ScenarioContext context, List<PassengerAgent> passengers) {
        return passengers.stream()
                .mapToDouble(passenger -> passengerDecisionEngine.calculateCancellationRisk(context, passenger, 180))
                .average()
                .orElse(0.1);
    }
}
