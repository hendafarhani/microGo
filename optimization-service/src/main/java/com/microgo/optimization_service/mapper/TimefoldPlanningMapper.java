package com.microgo.optimization_service.mapper;

import com.microgo.optimization_service.domain.DriverSnapshot;
import com.microgo.optimization_service.domain.OptimizationComparison;
import com.microgo.optimization_service.domain.OptimizationSnapshot;
import com.microgo.optimization_service.domain.RideRequestSnapshot;
import com.microgo.optimization_service.enums.ZoneId;
import com.microgo.optimization_service.domain.DriverPlanningEntity;
import com.microgo.optimization_service.domain.DriverRepositioningPlan;
import com.microgo.optimization_service.domain.RideAvailabilityOptimizationSolution;
import com.microgo.optimization_service.domain.RideRequestPlanningEntity;
import com.microgo.optimization_service.domain.ScenarioPlanningFact;
import com.microgo.optimization_service.domain.ZonePlanningFact;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class TimefoldPlanningMapper {

    private static final String SOLVER_STATUS = "HEURISTIC_BEST_SOLUTION_FOUND";
    private static final String SCORE_SUMMARY =
            "Heuristic driver repositioning plan generated behind Timefold service boundary";

    public List<DriverPlanningEntity> toDriverPlanningEntities(List<DriverSnapshot> drivers) {
        return drivers.stream().map(this::toDriverPlanningEntity).toList();
    }

    public List<RideRequestPlanningEntity> toRideRequestPlanningEntities(List<RideRequestSnapshot> requests) {
        return requests.stream()
                .map(request -> RideRequestPlanningEntity.builder()
                        .rideId(request.getRideId())
                        .pickupZone(request.getPickupZone())
                        .destinationZone(request.getDestinationZone())
                        .build())
                .toList();
    }

    public DriverRepositioningPlan toRepositioningPlan(
            DriverSnapshot driver,
            ZoneId targetZone,
            double priorityScore,
            int expectedWaitTimeReductionSeconds,
            double expectedCancellationReduction) {
        return DriverRepositioningPlan.builder()
                .driver(toDriverPlanningEntity(driver))
                .targetZone(targetZone)
                .priorityScore(priorityScore)
                .expectedWaitTimeReductionSeconds(expectedWaitTimeReductionSeconds)
                .expectedCancellationReduction(expectedCancellationReduction)
                .build();
    }

    public RideAvailabilityOptimizationSolution toProvisionalSolution(
            OptimizationSnapshot snapshot,
            Map<ZoneId, Integer> demandByZone,
            Map<ZoneId, Integer> initialSupplyByZone,
            List<DriverRepositioningPlan> repositioningPlans) {
        return RideAvailabilityOptimizationSolution.builder()
                .drivers(toDriverPlanningEntities(snapshot.getDriverSnapshots()))
                .rideRequests(toRideRequestPlanningEntities(snapshot.getPendingRideRequests()))
                .zones(toZonePlanningFacts(demandByZone, initialSupplyByZone))
                .scenario(toScenarioPlanningFact(snapshot))
                .distanceMatrix(snapshot.getDistanceMatrix())
                .driverRepositioningPlans(repositioningPlans)
                .solverStatus(SOLVER_STATUS)
                .scoreHard(0)
                .scoreSoft(sumPlanPriorityScores(repositioningPlans))
                .scoreSummary(SCORE_SUMMARY)
                .build();
    }

    public RideAvailabilityOptimizationSolution attachComparison(
            RideAvailabilityOptimizationSolution solution,
            OptimizationComparison comparison,
            int comparisonScore) {
        return solution.toBuilder()
                .scoreSoft(comparisonScore)
                .comparison(comparison)
                .build();
    }

    private DriverPlanningEntity toDriverPlanningEntity(DriverSnapshot driver) {
        return DriverPlanningEntity.builder()
                .driverId(driver.getDriverId())
                .currentZone(driver.getCurrentZone())
                .status(driver.getStatus())
                .available(driver.isAvailable())
                .fatigueScore(driver.getFatigueScore())
                .acceptanceProbability(driver.getAcceptanceProbability())
                .build();
    }

    private ScenarioPlanningFact toScenarioPlanningFact(OptimizationSnapshot snapshot) {
        return ScenarioPlanningFact.builder()
                .scenarioType(snapshot.getActiveScenario())
                .trafficMultiplier(snapshot.getTrafficMultiplier())
                .cancellationRisk(snapshot.getCancellationRisk())
                .acceptanceProbability(snapshot.getDriverAcceptanceProbability())
                .build();
    }

    private List<ZonePlanningFact> toZonePlanningFacts(
            Map<ZoneId, Integer> demandByZone,
            Map<ZoneId, Integer> supplyByZone) {
        List<ZonePlanningFact> zoneFacts = new ArrayList<>();
        for (ZoneId zone : ZoneId.values()) {
            zoneFacts.add(ZonePlanningFact.builder()
                    .zoneId(zone)
                    .predictedDemand(demandByZone.getOrDefault(zone, 0))
                    .currentDriverSupply(supplyByZone.getOrDefault(zone, 0))
                    .build());
        }
        return zoneFacts;
    }

    private int sumPlanPriorityScores(List<DriverRepositioningPlan> repositioningPlans) {
        return (int) Math.round(repositioningPlans.stream()
                .mapToDouble(DriverRepositioningPlan::getPriorityScore)
                .sum());
    }
}
