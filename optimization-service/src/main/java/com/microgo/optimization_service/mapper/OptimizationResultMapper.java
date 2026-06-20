package com.microgo.optimization_service.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microgo.optimization_service.domain.DriverRecommendation;
import com.microgo.optimization_service.domain.OptimizationComparison;
import com.microgo.optimization_service.domain.OptimizationMetrics;
import com.microgo.optimization_service.domain.OptimizationRunView;
import com.microgo.optimization_service.domain.OptimizationSnapshot;
import com.microgo.optimization_service.enums.OptimizationTrigger;
import com.microgo.optimization_service.enums.ZoneId;
import com.microgo.optimization_service.entity.BaselineVsOptimizedMetricsEntity;
import com.microgo.optimization_service.entity.DriverRepositioningRecommendationEntity;
import com.microgo.optimization_service.entity.OptimizationResultEntity;
import com.microgo.optimization_service.entity.OptimizationRunEntity;
import com.microgo.optimization_service.kafka.model.DriverRepositioningRecommendedEvent;
import com.microgo.optimization_service.kafka.model.OptimizedAssignmentRecommendedEvent;
import com.microgo.optimization_service.kafka.model.OptimizationCompletedEvent;
import com.microgo.optimization_service.kafka.model.OptimizationRequestedEvent;
import com.microgo.optimization_service.service.BaselineVsOptimizedComparator;
import com.microgo.optimization_service.domain.DriverRepositioningPlan;
import com.microgo.optimization_service.domain.RideAvailabilityOptimizationSolution;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OptimizationResultMapper {

    private final ObjectMapper objectMapper;
    private final BaselineVsOptimizedComparator comparator;

    public OptimizationRunEntity newRunEntity(UUID runId, OptimizationSnapshot snapshot, OptimizationTrigger trigger, Instant startedAt) {
        return OptimizationRunEntity.builder()
                .id(runId)
                .simulationRunId(snapshot.getSimulationRunId())
                .scenario(snapshot.getActiveScenario())
                .triggerSource(trigger)
                .solverStatus("STARTED")
                .snapshotGeneratedAt(snapshot.getSnapshotGeneratedAt())
                .startedAt(startedAt)
                .baselineStrategy("NEAREST_5_DRIVERS")
                .optimizationStrategy("TIMEFOLD_DRIVER_REPOSITIONING")
                .build();
    }

    public void completeRunEntity(OptimizationRunEntity runEntity, RideAvailabilityOptimizationSolution solution, Instant completedAt) {
        runEntity.setSolverStatus(solution.getSolverStatus());
        runEntity.setScoreSummary(solution.getScoreSummary());
        runEntity.setCompletedAt(completedAt);
    }

    public OptimizationResultEntity toResultEntity(OptimizationRunEntity runEntity,
                                                   OptimizationSnapshot snapshot,
                                                   RideAvailabilityOptimizationSolution solution,
                                                   Instant createdAt) {
        return OptimizationResultEntity.builder()
                .id(UUID.randomUUID())
                .optimizationRun(runEntity)
                .driverCount(snapshot.getDriverSnapshots().size())
                .pendingRideCount(snapshot.getPendingRideRequests().size())
                .demandSummary(toJson(snapshot.getPredictedDemandByZone()))
                .baselineMetrics(toJson(solution.getComparison().getBaselineMetrics()))
                .optimizedMetrics(toJson(solution.getComparison().getOptimizedMetrics()))
                .scoreHard(solution.getScoreHard())
                .scoreSoft(solution.getScoreSoft())
                .createdAt(createdAt)
                .build();
    }

    public List<BaselineVsOptimizedMetricsEntity> toMetricEntities(OptimizationRunEntity runEntity,
                                                                   OptimizationComparison comparison) {
        List<BaselineVsOptimizedMetricsEntity> metrics = new ArrayList<>();
        metrics.add(metric(runEntity, "average_wait_seconds",
                comparison.getBaselineMetrics().getAverageWaitSeconds(),
                comparison.getOptimizedMetrics().getAverageWaitSeconds(), "seconds"));
        metrics.add(metric(runEntity, "p95_wait_seconds",
                comparison.getBaselineMetrics().getP95WaitSeconds(),
                comparison.getOptimizedMetrics().getP95WaitSeconds(), "seconds"));
        metrics.add(metric(runEntity, "cancellation_risk",
                comparison.getBaselineMetrics().getCancellationRisk(),
                comparison.getOptimizedMetrics().getCancellationRisk(), "ratio"));
        metrics.add(metric(runEntity, "high_demand_coverage_ratio",
                comparison.getBaselineMetrics().getHighDemandCoverageRatio(),
                comparison.getOptimizedMetrics().getHighDemandCoverageRatio(), "ratio"));
        metrics.add(metric(runEntity, "unmet_request_count",
                comparison.getBaselineMetrics().getUnmetRequestCount(),
                comparison.getOptimizedMetrics().getUnmetRequestCount(), "count"));
        return metrics;
    }

    public List<DriverRepositioningRecommendationEntity> toRecommendationEntities(OptimizationRunEntity runEntity,
                                                                                  RideAvailabilityOptimizationSolution solution,
                                                                                  Instant createdAt) {
        return solution.getDriverRepositioningPlans().stream()
                .filter(DriverRepositioningPlan::isMoveRecommended)
                .map(plan -> DriverRepositioningRecommendationEntity.builder()
                        .id(UUID.randomUUID())
                        .optimizationRun(runEntity)
                        .driverId(plan.getDriver().getDriverId())
                        .currentZone(plan.getDriver().getCurrentZone().name())
                        .targetZone(plan.getTargetZone().name())
                        .distanceKm(solution.getDistanceMatrix()
                                .distanceKilometers(plan.getDriver().getCurrentZone(), plan.getTargetZone()))
                        .priorityScore(plan.getPriorityScore())
                        .expectedWaitReductionSeconds(plan.getExpectedWaitTimeReductionSeconds())
                        .expectedCancellationReduction(plan.getExpectedCancellationReduction())
                        .recommendationStatus("RECOMMENDED")
                        .createdAt(createdAt)
                        .build())
                .toList();
    }

    public OptimizationRequestedEvent toRequestedEvent(UUID runId,
                                                       OptimizationSnapshot snapshot,
                                                       OptimizationTrigger trigger,
                                                       Instant requestedAt) {
        return OptimizationRequestedEvent.builder()
                .optimizationRunId(runId)
                .simulationRunId(snapshot.getSimulationRunId())
                .scenario(snapshot.getActiveScenario())
                .requestedAt(requestedAt)
                .trigger(trigger)
                .build();
    }

    public List<DriverRepositioningRecommendedEvent> toDriverRecommendationEvents(UUID runId,
                                                                                  OptimizationSnapshot snapshot,
                                                                                  RideAvailabilityOptimizationSolution solution,
                                                                                  Instant recommendedAt) {
        return solution.getDriverRepositioningPlans().stream()
                .filter(DriverRepositioningPlan::isMoveRecommended)
                .map(plan -> DriverRepositioningRecommendedEvent.builder()
                        .optimizationRunId(runId)
                        .driverId(plan.getDriver().getDriverId())
                        .scenario(snapshot.getActiveScenario())
                        .fromZone(plan.getDriver().getCurrentZone())
                        .targetZone(plan.getTargetZone())
                        .priorityScore(plan.getPriorityScore())
                        .expectedWaitTimeReductionSeconds(plan.getExpectedWaitTimeReductionSeconds())
                        .expectedCancellationReduction(plan.getExpectedCancellationReduction())
                        .recommendedAt(recommendedAt)
                        .build())
                .toList();
    }

    public List<OptimizedAssignmentRecommendedEvent> toAssignmentRecommendationEvents(UUID runId,
                                                                                      OptimizationSnapshot snapshot,
                                                                                      RideAvailabilityOptimizationSolution solution,
                                                                                      int baselineDriverLimit,
                                                                                      Instant recommendedAt) {
        Map<String, ZoneId> optimizedTargets = solution.getDriverRepositioningPlans().stream()
                .filter(DriverRepositioningPlan::isMoveRecommended)
                .collect(Collectors.toMap(plan -> plan.getDriver().getDriverId(), DriverRepositioningPlan::getTargetZone));

        return snapshot.getPendingRideRequests().stream()
                .map(request -> {
                    List<String> baselineCandidates = comparator
                            .findBaselineNearestDriverIds(snapshot, request.getPickupZone(), baselineDriverLimit);
                    String recommendedDriverId = snapshot.getDriverSnapshots().stream()
                            .filter(driver -> driver.isRepositionable())
                            .sorted((left, right) -> Integer.compare(
                                    snapshot.getDistanceMatrix().travelMinutes(
                                            optimizedTargets.getOrDefault(left.getDriverId(), left.getCurrentZone()),
                                            request.getPickupZone()),
                                    snapshot.getDistanceMatrix().travelMinutes(
                                            optimizedTargets.getOrDefault(right.getDriverId(), right.getCurrentZone()),
                                            request.getPickupZone())))
                            .map(driver -> driver.getDriverId())
                            .findFirst()
                            .orElse(null);
                    if (recommendedDriverId == null) {
                        return null;
                    }
                    int expectedPickupEtaSeconds = snapshot.getDistanceMatrix().travelMinutes(
                            optimizedTargets.getOrDefault(recommendedDriverId,
                                    snapshot.getDriverSnapshots().stream()
                                            .filter(driver -> driver.getDriverId().equals(recommendedDriverId))
                                            .findFirst()
                                            .map(driver -> driver.getCurrentZone())
                                            .orElse(ZoneId.GENERAL_LONDON)),
                            request.getPickupZone()) * 60;
                    return OptimizedAssignmentRecommendedEvent.builder()
                            .optimizationRunId(runId)
                            .rideId(request.getRideId())
                            .recommendedDriverId(recommendedDriverId)
                            .baselineCandidateDriverIds(baselineCandidates)
                            .expectedPickupEtaSeconds(expectedPickupEtaSeconds)
                            .recommendedAt(recommendedAt)
                            .build();
                })
                .filter(event -> event != null)
                .toList();
    }

    public OptimizationCompletedEvent toCompletedEvent(UUID runId,
                                                       OptimizationSnapshot snapshot,
                                                       RideAvailabilityOptimizationSolution solution,
                                                       Instant completedAt) {
        return OptimizationCompletedEvent.builder()
                .optimizationRunId(runId)
                .simulationRunId(snapshot.getSimulationRunId())
                .scenario(snapshot.getActiveScenario())
                .solverStatus(solution.getSolverStatus())
                .baselineAverageWaitSeconds(solution.getComparison().getBaselineMetrics().getAverageWaitSeconds())
                .optimizedAverageWaitSeconds(solution.getComparison().getOptimizedMetrics().getAverageWaitSeconds())
                .baselineCancellationRisk(solution.getComparison().getBaselineMetrics().getCancellationRisk())
                .optimizedCancellationRisk(solution.getComparison().getOptimizedMetrics().getCancellationRisk())
                .completedAt(completedAt)
                .build();
    }

    public OptimizationRunView toView(OptimizationRunEntity runEntity,
                                      OptimizationResultEntity resultEntity,
                                      List<DriverRepositioningRecommendationEntity> recommendationEntities) {
        return OptimizationRunView.builder()
                .optimizationRunId(runEntity.getId())
                .simulationRunId(runEntity.getSimulationRunId())
                .scenario(runEntity.getScenario())
                .solverStatus(runEntity.getSolverStatus())
                .baselineMetrics(fromJson(resultEntity.getBaselineMetrics()))
                .optimizedMetrics(fromJson(resultEntity.getOptimizedMetrics()))
                .recommendations(recommendationEntities.stream()
                        .map(entity -> DriverRecommendation.builder()
                                .driverId(entity.getDriverId())
                                .currentZone(ZoneId.valueOf(entity.getCurrentZone()))
                                .targetZone(ZoneId.valueOf(entity.getTargetZone()))
                                .distanceKm(entity.getDistanceKm())
                                .priorityScore(entity.getPriorityScore())
                                .expectedWaitReductionSeconds(entity.getExpectedWaitReductionSeconds() == null ? 0 : entity.getExpectedWaitReductionSeconds())
                                .expectedCancellationReduction(entity.getExpectedCancellationReduction() == null ? 0.0 : entity.getExpectedCancellationReduction())
                                .build())
                        .toList())
                .startedAt(runEntity.getStartedAt())
                .completedAt(runEntity.getCompletedAt())
                .build();
    }

    private BaselineVsOptimizedMetricsEntity metric(OptimizationRunEntity runEntity,
                                                    String name,
                                                    double baseline,
                                                    double optimized,
                                                    String unit) {
        return BaselineVsOptimizedMetricsEntity.builder()
                .id(UUID.randomUUID())
                .optimizationRun(runEntity)
                .metricName(name)
                .baselineValue(baseline)
                .optimizedValue(optimized)
                .improvement(baseline - optimized)
                .unit(unit)
                .build();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize optimization payload", exception);
        }
    }

    private OptimizationMetrics fromJson(String value) {
        try {
            return objectMapper.readValue(value, OptimizationMetrics.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize optimization metrics", exception);
        }
    }
}
