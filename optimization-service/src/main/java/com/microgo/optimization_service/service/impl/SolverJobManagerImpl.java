package com.microgo.optimization_service.service.impl;

import com.microgo.optimization_service.config.OptimizationServiceProperties;
import com.microgo.optimization_service.domain.OptimizationRunView;
import com.microgo.optimization_service.domain.OptimizationSnapshot;
import com.microgo.optimization_service.enums.OptimizationTrigger;
import com.microgo.optimization_service.enums.ScenarioType;
import com.microgo.optimization_service.entity.OptimizationResultEntity;
import com.microgo.optimization_service.entity.OptimizationRunEntity;
import com.microgo.optimization_service.mapper.OptimizationResultMapper;
import com.microgo.optimization_service.repository.BaselineVsOptimizedMetricsRepository;
import com.microgo.optimization_service.repository.DriverRepositioningRecommendationRepository;
import com.microgo.optimization_service.repository.OptimizationResultRepository;
import com.microgo.optimization_service.repository.OptimizationRunRepository;
import com.microgo.optimization_service.service.OptimizationSnapshotBuilder;
import com.microgo.optimization_service.kafka.publisher.RecommendationPublisher;
import com.microgo.optimization_service.service.SolverJobManager;
import com.microgo.optimization_service.service.TimefoldSolverService;
import com.microgo.optimization_service.domain.RideAvailabilityOptimizationSolution;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class SolverJobManagerImpl implements SolverJobManager {

    private final OptimizationSnapshotBuilder snapshotBuilder;
    private final TimefoldSolverService timefoldSolverService;
    private final RecommendationPublisher recommendationPublisher;
    private final OptimizationResultMapper mapper;
    private final OptimizationRunRepository optimizationRunRepository;
    private final OptimizationResultRepository optimizationResultRepository;
    private final BaselineVsOptimizedMetricsRepository metricsRepository;
    private final DriverRepositioningRecommendationRepository recommendationRepository;
    private final OptimizationServiceProperties properties;

    public SolverJobManagerImpl(OptimizationSnapshotBuilder snapshotBuilder,
                                TimefoldSolverService timefoldSolverService,
                                RecommendationPublisher recommendationPublisher,
                                OptimizationResultMapper mapper,
                                OptimizationRunRepository optimizationRunRepository,
                                OptimizationResultRepository optimizationResultRepository,
                                BaselineVsOptimizedMetricsRepository metricsRepository,
                                DriverRepositioningRecommendationRepository recommendationRepository,
                                OptimizationServiceProperties properties) {
        this.snapshotBuilder = snapshotBuilder;
        this.timefoldSolverService = timefoldSolverService;
        this.recommendationPublisher = recommendationPublisher;
        this.mapper = mapper;
        this.optimizationRunRepository = optimizationRunRepository;
        this.optimizationResultRepository = optimizationResultRepository;
        this.metricsRepository = metricsRepository;
        this.recommendationRepository = recommendationRepository;
        this.properties = properties;
    }

    @Override
    @Transactional
    public UUID runOptimization(UUID simulationRunId, ScenarioType scenarioType, OptimizationTrigger trigger) {
        OptimizationSnapshot snapshot = snapshotBuilder.buildOptimizationSnapshot(simulationRunId, scenarioType);
        UUID optimizationRunId = UUID.randomUUID();
        Instant startedAt = Instant.now();
        OptimizationRunEntity runEntity = createAndPersistRun(
                optimizationRunId,
                snapshot,
                trigger,
                startedAt);
        publishOptimizationRequested(optimizationRunId, snapshot, trigger, startedAt);

        RideAvailabilityOptimizationSolution solution = timefoldSolverService.solveSnapshot(snapshot);
        Instant completedAt = Instant.now();
        completeAndPersistRun(runEntity, solution, completedAt);
        persistOptimizationArtifacts(runEntity, snapshot, solution, completedAt);
        publishOptimizationRecommendations(
                optimizationRunId,
                snapshot,
                solution,
                completedAt);

        return optimizationRunId;
    }

    @Override
    @Transactional
    public OptimizationRunView getRun(UUID optimizationRunId) {
        return mapRunView(loadRun(optimizationRunId));
    }

    @Override
    @Transactional
    public OptimizationRunView latestForScenario(ScenarioType scenarioType) {
        OptimizationRunEntity runEntity = optimizationRunRepository.findTopByScenarioOrderByStartedAtDesc(scenarioType)
                .orElseThrow(() -> new IllegalArgumentException("No optimization run found for scenario " + scenarioType));
        return mapRunView(runEntity);
    }

    private OptimizationRunEntity createAndPersistRun(
            UUID optimizationRunId,
            OptimizationSnapshot snapshot,
            OptimizationTrigger trigger,
            Instant startedAt) {
        OptimizationRunEntity runEntity =
                mapper.newRunEntity(optimizationRunId, snapshot, trigger, startedAt);
        return optimizationRunRepository.save(runEntity);
    }

    private void publishOptimizationRequested(
            UUID optimizationRunId,
            OptimizationSnapshot snapshot,
            OptimizationTrigger trigger,
            Instant startedAt) {
        recommendationPublisher.publishRequested(
                mapper.toRequestedEvent(optimizationRunId, snapshot, trigger, startedAt));
    }

    private void completeAndPersistRun(
            OptimizationRunEntity runEntity,
            RideAvailabilityOptimizationSolution solution,
            Instant completedAt) {
        mapper.completeRunEntity(runEntity, solution, completedAt);
        optimizationRunRepository.save(runEntity);
    }

    private void persistOptimizationArtifacts(
            OptimizationRunEntity runEntity,
            OptimizationSnapshot snapshot,
            RideAvailabilityOptimizationSolution solution,
            Instant completedAt) {
        optimizationResultRepository.save(
                mapper.toResultEntity(runEntity, snapshot, solution, completedAt));
        metricsRepository.saveAll(mapper.toMetricEntities(runEntity, solution.getComparison()));
        recommendationRepository.saveAll(
                mapper.toRecommendationEntities(runEntity, solution, completedAt));
    }

    private void publishOptimizationRecommendations(
            UUID optimizationRunId,
            OptimizationSnapshot snapshot,
            RideAvailabilityOptimizationSolution solution,
            Instant completedAt) {
        recommendationPublisher.publishDriverRecommendations(
                mapper.toDriverRecommendationEvents(
                        optimizationRunId,
                        snapshot,
                        solution,
                        completedAt));
        recommendationPublisher.publishAssignmentRecommendations(
                mapper.toAssignmentRecommendationEvents(
                        optimizationRunId,
                        snapshot,
                        solution,
                        properties.getMaxNearestDrivers(),
                        completedAt));
        recommendationPublisher.publishCompleted(
                mapper.toCompletedEvent(optimizationRunId, snapshot, solution, completedAt));
    }

    private OptimizationRunEntity loadRun(UUID optimizationRunId) {
        return optimizationRunRepository.findById(optimizationRunId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Optimization run not found: " + optimizationRunId));
    }

    private OptimizationRunView mapRunView(OptimizationRunEntity runEntity) {
        OptimizationResultEntity resultEntity = optimizationResultRepository
                .findByOptimizationRunId(runEntity.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Optimization result not found for: " + runEntity.getId()));
        return mapper.toView(
                runEntity,
                resultEntity,
                recommendationRepository.findByOptimizationRunId(runEntity.getId()));
    }
}
