package com.microgo.optimization_service.service.impl;

import com.microgo.optimization_service.businessrule.SimulationDemandBusinessRules;
import com.microgo.optimization_service.config.OptimizationServiceProperties;
import com.microgo.optimization_service.enums.ScenarioType;
import com.microgo.optimization_service.enums.ZoneId;
import com.microgo.optimization_service.kafka.model.ScenarioStartedEvent;
import com.microgo.optimization_service.kafka.model.SimulationCompletedEvent;
import com.microgo.optimization_service.kafka.model.SimulationMetricsUpdatedEvent;
import com.microgo.optimization_service.mapper.SimulationStateMapper;
import com.microgo.optimization_service.service.SimulationOutputReader;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class SimulationOutputReaderImpl implements SimulationOutputReader {

    private final OptimizationServiceProperties properties;
    private final SimulationStateMapper simulationStateMapper;
    private final Map<UUID, SimulationState> simulationStates = new ConcurrentHashMap<>();
    private final AtomicReference<UUID> activeSimulationRunId = new AtomicReference<>();

    public SimulationOutputReaderImpl(
            OptimizationServiceProperties properties,
            SimulationStateMapper simulationStateMapper) {
        this.properties = properties;
        this.simulationStateMapper = simulationStateMapper;
    }

    @Override
    public void onScenarioStarted(ScenarioStartedEvent event) {
        storeAsActive(
                event.getSimulationRunId(),
                simulationStateMapper.fromScenarioStarted(
                        event,
                        SimulationDemandBusinessRules.inferDemandByScenario(event.getScenario(), 0)));
    }

    @Override
    public void onSimulationMetricsUpdated(SimulationMetricsUpdatedEvent event) {
        SimulationState currentState = simulationStates.get(event.getSimulationRunId());
        Map<ZoneId, Integer> predictedDemand = preserveOrInferDemand(currentState, event);
        storeAsActive(
                event.getSimulationRunId(),
                simulationStateMapper.fromMetricsUpdated(event, predictedDemand));
    }

    @Override
    public void onSimulationCompleted(SimulationCompletedEvent event) {
        storeAsActive(
                event.getSimulationRunId(),
                simulationStateMapper.fromSimulationCompleted(
                        event,
                        normalizeDemandByZone(event.getPredictedDemandByZone())));
    }

    @Override
    public Optional<SimulationState> findSimulationState(UUID simulationRunId) {
        UUID targetRunId = resolveTargetRunId(simulationRunId);
        if (targetRunId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(simulationStates.get(targetRunId));
    }

    @Override
    public double resolveTrafficMultiplier(ScenarioType scenarioType) {
        return SimulationDemandBusinessRules.resolveTrafficMultiplier(
                scenarioType,
                properties.getConcertRainTrafficMultiplier(),
                properties.getAirportRushTrafficMultiplier());
    }

    private void storeAsActive(UUID simulationRunId, SimulationState simulationState) {
        simulationStates.put(simulationRunId, simulationState);
        activeSimulationRunId.set(simulationRunId);
    }

    private UUID resolveTargetRunId(UUID requestedRunId) {
        return requestedRunId != null ? requestedRunId : activeSimulationRunId.get();
    }

    private Map<ZoneId, Integer> preserveOrInferDemand(
            SimulationState currentState,
            SimulationMetricsUpdatedEvent event) {
        if (currentState != null
                && currentState.getPredictedDemandByZone() != null
                && !currentState.getPredictedDemandByZone().isEmpty()) {
            return currentState.getPredictedDemandByZone();
        }
        return SimulationDemandBusinessRules.inferDemandByScenario(event.getScenario(), event.getPendingRideRequests());
    }

    private Map<ZoneId, Integer> normalizeDemandByZone(Map<String, Integer> rawDemand) {
        Map<ZoneId, Integer> normalizedDemand = new EnumMap<>(ZoneId.class);
        for (ZoneId zoneId : ZoneId.values()) {
            normalizedDemand.put(zoneId, 0);
        }
        rawDemand.forEach((zone, demand) -> normalizedDemand.put(ZoneId.valueOf(zone), demand));
        return normalizedDemand;
    }

}
