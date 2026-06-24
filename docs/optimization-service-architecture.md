# Optimization Service

## Architecture Proposal

`optimization-service` should be a dedicated Spring Boot microservice that consumes simulation output, merges it with live driver state, runs baseline and optimized ride availability analysis, and publishes repositioning or assignment recommendations. It should not own ride lifecycle state, synthetic scenario generation, or physical driver movement. Its responsibility is to improve likely outcomes, not predict them or execute them.

### Target Services And Why

- `simulation-service`
  - owns scenario execution, ride demand simulation, forecasting metrics, and optimization-facing predicted state
- `optimization-service`
  - owns optimization snapshots, baseline comparison, Timefold planning runs, recommendation generation, and optimization result persistence
- `driver-location-generator`
  - owns Redis GEO live driver state and execution of repositioning movement after recommendations become commands
- `ride-request`
  - remains the production dispatch owner and keeps the baseline nearest-five-driver strategy during the first implementation phase
- `dashboard-service` or future operations UI
  - may consume optimization results for operator visibility, but does not make planning decisions

### Event And Data Flow

1. `simulation-service` publishes `ScenarioStartedEvent`, `SimulatedRideRequestedEvent`, `SimulationMetricsUpdatedEvent`, and `SimulationCompletedEvent`.
2. `driver-location-generator` publishes `DriverLocationUpdatedEvent` keyed by `driverId` and maintains Redis GEO `vehicle_location` plus current driver geo-state records.
3. `ride-request` continues to publish `RideAssignedEvent` and `RideCancelledEvent`.
4. `SimulationOutputReader`, `DriverSnapshotReader`, and `RideRequestSnapshotReader` build a current optimization view from Kafka-fed state plus Redis.
5. `DistanceMatrixSnapshotReader` resolves zone-to-zone travel cost from a London matrix and applies traffic multipliers from the active scenario snapshot.
6. `OptimizationSnapshotBuilder` assembles an `OptimizationSnapshot` that includes:
   - active scenario
   - current driver positions
   - driver statuses
   - pending ride requests
   - predicted demand by zone
   - traffic multiplier
   - cancellation risk
   - acceptance probability
   - London distance matrix
7. `BaselineVsOptimizedComparator` computes the nearest-five baseline using the same driver state visible to the optimizer.
8. `TimefoldSolverService` runs the first optimization phase for driver repositioning and stores the best solution plus score explanation.
9. `RecommendationPublisher` publishes:
   - `OptimizationRequestedEvent`
   - `DriverRepositioningRecommendedEvent`
   - `OptimizationCompletedEvent`
10. `driver-location-generator` later consumes repositioning commands derived from the recommendation stream and owns actual movement execution.
11. `optimization-service` persists run metadata, per-driver recommendations, and baseline-vs-optimized metrics in PostgreSQL.
12. REST endpoints expose manual optimization triggers and historical result lookup.

### First-Phase Scope

- Optimize driver repositioning before demand spikes.
- Keep `ride-request` unchanged as the dispatch decision maker.
- Publish `OptimizedAssignmentRecommendedEvent` only as a recommendation artifact in phase one, not as a command that overrides the live assignment path.
- Treat multi-stop or TSP-style routing as explicitly deferred unless a future scenario requires batched driver errands or hot-zone tour ordering.

### Risk Hotspots

- Snapshot freshness: the optimization run must record the event and Redis timestamps it used so stale recommendations can be detected.
- Per-driver ordering: `DriverLocationUpdatedEvent`, `RideAssignedEvent`, and repositioning recommendations should stay keyed by `driverId` where possible.
- Zone drift: the zone taxonomy used by simulation, optimization, and movement execution must remain identical.
- Recommendation loops: a reposition recommendation must not be republished endlessly if a driver is already moving toward the same target zone.
- Service boundary creep: `optimization-service` should not start moving drivers directly or replacing `ride-request` assignment ownership during the first phase.

### Implementation Constraints

- Use PostgreSQL for durable optimization history and comparison results.
- Use Redis as a read-side live geo-state source, not as the durable optimization ledger.
- Keep Kafka handlers thin and idempotent; snapshot state should be updated in dedicated services.
- Keep the first version synchronous enough for operator-triggered or scenario-triggered runs, but isolate solver orchestration behind a job manager so asynchronous execution can be added later.
- Keep Timefold models zone-oriented first. Do not model full street-level routing until the current zone-based optimization loop is stable.

## Package Structure

```text
optimization-service
├── businessrule
├── config
├── controller
├── domain
├── entity
├── kafka
│   ├── configuration
│   ├── consumer
│   ├── model
│   └── publisher
├── mapper
├── repository
├── service
│   ├── baseline
│   ├── snapshot
│   ├── solver
│   ├── serviceimpl
│   └── timefold
└── support
```

## Java Classes And Interfaces

### REST And Orchestration

- `OptimizationController`
  - `POST /api/v1/optimizations/run`
  - `GET /api/v1/optimizations/{runId}`
  - `GET /api/v1/optimizations/scenarios/{scenarioId}/latest`
- `SolverJobManager`
  - coordinates manual or event-driven solver jobs
- `TimefoldSolverService`
  - builds the planning problem, invokes Timefold, and returns the best solution
- `RecommendationPublisher`
  - publishes recommendation and completion events

Service-layer callers depend on these contracts rather than concrete classes. Spring-managed implementations live under `service/serviceimpl` with `Impl` suffixes, matching the repository's inversion-of-control convention.

Service implementations orchestrate state, calculations, persistence, and publication through intention-revealing methods. Domain, planning, event, and persistence object construction belongs in focused classes under `mapper`; service implementations should not call Lombok builders directly.

### Snapshot Assembly

- `OptimizationSnapshotBuilder`
- `DriverSnapshotReader`
- `SimulationOutputReader`
- `RideRequestSnapshotReader`
- `DistanceMatrixSnapshotReader`

### Planning Model

- `RideAvailabilityOptimizationSolution`
- `DriverRepositioningPlan`
- `DriverPlanningEntity`
- `RideRequestPlanningEntity`
- `ZonePlanningFact`
- `ScenarioPlanningFact`
- `DistanceMatrixFact`
- `RideDispatchConstraintProvider`

### Mapping And Comparison

- `DriverRepositioningBusinessRules`
- `OptimizationMetricsBusinessRules`
- `RideDispatchConstraintBusinessRules`
- `SimulationDemandBusinessRules`
- `ZoneDemandBusinessRules`
- `OptimizationResultMapper`
- `BaselineVsOptimizedComparator`

### Persistence

- `OptimizationRunEntity`
- `OptimizationResultEntity`
- `BaselineVsOptimizedMetricsEntity`
- `DriverRepositioningRecommendationEntity`
- `OptimizationRunRepository`
- `OptimizationResultRepository`
- `BaselineVsOptimizedMetricsRepository`
- `DriverRepositioningRecommendationRepository`

## Minimal Spring Boot Skeleton

```text
optimization-service
├── src/main/java/com/microgo/optimization_service
│   ├── OptimizationServiceApplication.java
│   ├── controller/OptimizationController.java
│   ├── domain/OptimizationSnapshot.java
│   ├── domain/ZoneDemandForecast.java
│   ├── domain/DriverSnapshot.java
│   ├── domain/RideRequestSnapshot.java
│   ├── kafka/model/OptimizationRequestedEvent.java
│   ├── kafka/model/OptimizationCompletedEvent.java
│   ├── kafka/model/DriverRepositioningRecommendedEvent.java
│   ├── kafka/model/OptimizedAssignmentRecommendedEvent.java
│   ├── service/snapshot/OptimizationSnapshotBuilder.java
│   ├── service/snapshot/DriverSnapshotReader.java
│   ├── service/snapshot/SimulationOutputReader.java
│   ├── service/snapshot/RideRequestSnapshotReader.java
│   ├── service/snapshot/DistanceMatrixSnapshotReader.java
│   ├── service/baseline/BaselineVsOptimizedComparator.java
│   ├── service/solver/TimefoldSolverService.java
│   ├── service/solver/SolverJobManager.java
│   ├── service/solver/RecommendationPublisher.java
│   ├── service/timefold/RideAvailabilityOptimizationSolution.java
│   ├── service/timefold/DriverRepositioningPlan.java
│   ├── service/timefold/DriverPlanningEntity.java
│   ├── service/timefold/RideRequestPlanningEntity.java
│   ├── service/timefold/ZonePlanningFact.java
│   ├── service/timefold/ScenarioPlanningFact.java
│   ├── service/timefold/DistanceMatrixFact.java
│   ├── service/timefold/RideDispatchConstraintProvider.java
│   ├── mapper/OptimizationResultMapper.java
│   ├── entity/OptimizationRunEntity.java
│   ├── entity/OptimizationResultEntity.java
│   ├── entity/BaselineVsOptimizedMetricsEntity.java
│   ├── entity/DriverRepositioningRecommendationEntity.java
│   └── repository/...
└── src/test/java/com/microgo/optimization_service/...
```

## Kafka Event Schemas

### Kafka Input

- `DriverLocationUpdatedEvent`
```json
{
  "driverId": "driver-1001",
  "providerIdentifier": "driver-1001",
  "scenario": "CONCERT_RAIN",
  "status": "CRUISING",
  "zone": "SOHO",
  "latitude": 51.5136,
  "longitude": -0.1365,
  "available": true,
  "tickSequence": 44,
  "occurredAt": "2026-06-18T21:30:00Z"
}
```

- `ScenarioStartedEvent`
```json
{
  "simulationRunId": "fd0dcb58-32d5-4d11-8f02-5b8ebaa1a675",
  "scenario": "CONCERT_RAIN",
  "startedAt": "2026-06-18T21:00:00Z"
}
```

- `SimulatedRideRequestedEvent`
```json
{
  "rideId": "ride-210",
  "simulationRunId": "fd0dcb58-32d5-4d11-8f02-5b8ebaa1a675",
  "passengerId": "passenger-77",
  "pickupZone": "WEMBLEY",
  "destinationZone": "SOHO",
  "pickupLatitude": 51.5569,
  "pickupLongitude": -0.2799,
  "requestedAt": "2026-06-18T21:31:10Z"
}
```

- `SimulationMetricsUpdatedEvent`
```json
{
  "simulationRunId": "fd0dcb58-32d5-4d11-8f02-5b8ebaa1a675",
  "scenario": "CONCERT_RAIN",
  "trafficMultiplier": 0.78,
  "predictedDemandByZone": {
    "WEMBLEY": 92,
    "SOHO": 28,
    "CAMDEN": 24,
    "KINGS_CROSS": 19
  },
  "acceptanceProbabilityByZone": {
    "WEMBLEY": 0.51,
    "SOHO": 0.74
  },
  "cancellationRiskByZone": {
    "WEMBLEY": 0.42,
    "SOHO": 0.16
  },
  "updatedAt": "2026-06-18T21:32:00Z"
}
```

- `SimulationCompletedEvent`
```json
{
  "simulationRunId": "fd0dcb58-32d5-4d11-8f02-5b8ebaa1a675",
  "scenario": "CONCERT_RAIN",
  "predictedDemandByZone": {
    "WEMBLEY": 120,
    "SOHO": 34,
    "CAMDEN": 31,
    "KINGS_CROSS": 26
  },
  "averageWaitingTimeSeconds": 510,
  "cancellationRisk": 0.38,
  "completedAt": "2026-06-18T21:45:00Z"
}
```

- `RideAssignedEvent`
- `RideCancelledEvent`

### Kafka Output

- `OptimizationRequestedEvent`
```json
{
  "optimizationRunId": "1f61b4d2-0afb-45f3-b59e-7b8f39e2e041",
  "simulationRunId": "fd0dcb58-32d5-4d11-8f02-5b8ebaa1a675",
  "scenario": "CONCERT_RAIN",
  "requestedAt": "2026-06-18T21:32:02Z",
  "trigger": "SIMULATION_METRICS_UPDATED"
}
```

- `DriverRepositioningRecommendedEvent`
```json
{
  "optimizationRunId": "1f61b4d2-0afb-45f3-b59e-7b8f39e2e041",
  "driverId": "driver-1001",
  "scenario": "CONCERT_RAIN",
  "fromZone": "SOHO",
  "targetZone": "WEMBLEY",
  "priorityScore": 0.89,
  "expectedWaitTimeReductionSeconds": 74,
  "expectedCancellationReduction": 0.08,
  "recommendedAt": "2026-06-18T21:32:04Z"
}
```

- `OptimizedAssignmentRecommendedEvent`
```json
{
  "optimizationRunId": "1f61b4d2-0afb-45f3-b59e-7b8f39e2e041",
  "rideId": "ride-210",
  "recommendedDriverId": "driver-1040",
  "baselineCandidateDriverIds": [
    "driver-1040",
    "driver-1092",
    "driver-1028",
    "driver-1110",
    "driver-1001"
  ],
  "expectedPickupEtaSeconds": 290,
  "recommendedAt": "2026-06-18T21:32:04Z"
}
```

- `OptimizationCompletedEvent`
```json
{
  "optimizationRunId": "1f61b4d2-0afb-45f3-b59e-7b8f39e2e041",
  "simulationRunId": "fd0dcb58-32d5-4d11-8f02-5b8ebaa1a675",
  "scenario": "CONCERT_RAIN",
  "solverStatus": "BEST_SOLUTION_FOUND",
  "baselineAverageWaitSeconds": 510,
  "optimizedAverageWaitSeconds": 432,
  "baselineCancellationRisk": 0.38,
  "optimizedCancellationRisk": 0.29,
  "completedAt": "2026-06-18T21:32:08Z"
}
```

## Database Schema

The first durable PostgreSQL schema should include:

- `optimization_run`
- `optimization_result`
- `baseline_vs_optimized_metrics`
- `driver_repositioning_recommendation`

```sql
create table optimization_run (
    id uuid primary key,
    simulation_run_id uuid not null,
    scenario varchar(64) not null,
    trigger_source varchar(64) not null,
    solver_status varchar(64) not null,
    snapshot_generated_at timestamptz not null,
    started_at timestamptz not null,
    completed_at timestamptz,
    baseline_strategy varchar(64) not null,
    optimization_strategy varchar(64) not null,
    score_summary text
);

create table optimization_result (
    id uuid primary key,
    optimization_run_id uuid not null references optimization_run(id),
    driver_count integer not null,
    pending_ride_count integer not null,
    demand_summary jsonb not null,
    baseline_metrics jsonb not null,
    optimized_metrics jsonb not null,
    score_hard integer,
    score_soft integer,
    created_at timestamptz not null
);

create table baseline_vs_optimized_metrics (
    id uuid primary key,
    optimization_run_id uuid not null references optimization_run(id),
    metric_name varchar(128) not null,
    baseline_value numeric(12,4) not null,
    optimized_value numeric(12,4) not null,
    improvement numeric(12,4) not null,
    unit varchar(32) not null
);

create table driver_repositioning_recommendation (
    id uuid primary key,
    optimization_run_id uuid not null references optimization_run(id),
    driver_id varchar(128) not null,
    current_zone varchar(64) not null,
    target_zone varchar(64) not null,
    distance_km numeric(8,3) not null,
    priority_score numeric(8,4) not null,
    expected_wait_reduction_seconds integer,
    expected_cancellation_reduction numeric(8,4),
    recommendation_status varchar(64) not null,
    created_at timestamptz not null
);

create index idx_optimization_run_simulation_run
    on optimization_run(simulation_run_id);

create index idx_driver_repositioning_recommendation_driver
    on driver_repositioning_recommendation(driver_id);
```

## Timefold Planning Model

### Planning Solution

- `RideAvailabilityOptimizationSolution`
  - planning entities: `DriverRepositioningPlan`
  - problem facts:
    - `ZonePlanningFact`
    - `ScenarioPlanningFact`
    - `DistanceMatrixFact`
    - immutable driver and ride request snapshot views
  - score type: `HardSoftScore`

### Planning Entity

- `DriverRepositioningPlan`
  - identifies one repositionable driver
  - keeps current zone, fatigue, acceptance tendency, and availability
  - planning variable: `targetZone`

### Planning Variable

- `targetZone`
  - nullable only when the driver should remain in place
  - drawn from active `ZonePlanningFact` values filtered by reachability and scenario policy

### Problem Facts

- current driver locations
- driver statuses
- predicted demand by zone
- pending ride requests
- scenario type
- traffic multiplier
- acceptance probability
- cancellation risk
- London distance matrix

### Hard Constraints

- offline drivers cannot be repositioned
- drivers with status `ON_TRIP` cannot be repositioned
- one driver can have only one target zone
- target zone must be reachable within the configured reposition horizon
- unavailable drivers cannot be considered assignment candidates

### Soft Constraints

- minimize distance from current zone to target zone
- maximize weighted coverage of high-demand zones
- avoid overcrowding one zone above a configurable saturation threshold
- keep a minimum idle supply in central London
- prefer low-fatigue drivers
- prefer drivers with higher scenario-specific acceptance probability
- reduce predicted waiting time
- reduce predicted cancellations

## VRP Formulation

The first implementation is a simplified zone-based VRP:

- vehicle
  - one available driver
- customer demand
  - one unit of expected coverage need in a forecast zone
- depot
  - the driver’s current zone
- destination
  - a target supply zone
- travel cost
  - London zone-to-zone travel time from `DistanceMatrixFact`, adjusted by traffic multiplier
- service reward
  - expected wait-time reduction and cancellation-risk reduction for the target zone
- capacity
  - one driver can satisfy one repositioning decision per optimization run

This keeps the model close to VRP reasoning while avoiding premature street-level routing complexity. TSP remains optional for future hot-zone tour ordering or multi-stop staging.

## Metaheuristics Strategy

Use Timefold with a staged solver configuration:

1. Construction heuristic
   - first-fit decreasing on zones ordered by demand pressure minus current supply
2. Local search
   - hill climbing or late acceptance for fast early improvements
3. Tabu search
   - prevents oscillation between the same London zones when pressure is noisy
4. Simulated annealing
   - helps escape local optima in scenarios like `CONCERT_RAIN` where Wembley pressure can dominate too aggressively
5. Genetic algorithm
   - defer until scenario size or multi-objective complexity justifies it; document as phase-two experimentation rather than first release behavior

Recommended first release: construction heuristic plus tabu search, with simulated annealing as an optional profile when benchmark results show premature convergence.

## Baseline Vs Optimized Comparison Logic

### Baseline

- replicate the current `ride-request` assumption: nearest five available drivers per pending request
- estimate pickup ETA from the same distance matrix and current driver zone
- project zone depletion after candidate allocation
- aggregate expected average wait time, unmet demand count, and cancellation exposure

### Optimized

- apply solver-selected repositioning targets
- recompute zone supply before the same pending-demand evaluation
- estimate improved pickup ETA and coverage
- compare results on:
  - average wait seconds
  - p95 wait seconds
  - unmet request count
  - weighted cancellation risk
  - high-demand zone coverage ratio
  - central London minimum supply compliance

### Comparison Guardrails

- baseline and optimized runs must use the same snapshot timestamp
- ignore drivers who changed status during the run and flag the result as partially stale
- persist both raw metrics and percentage improvement for auditability

## Sequence Flow With Simulation Agent

1. `simulation-service` publishes `ScenarioStartedEvent`.
2. `optimization-service` opens or refreshes scenario-scoped snapshot state.
3. `simulation-service` publishes `SimulatedRideRequestedEvent` for predicted demand pressure.
4. `optimization-service` records pending request projections by zone.
5. `simulation-service` publishes `SimulationMetricsUpdatedEvent` with traffic, demand, acceptance, and cancellation signals.
6. `optimization-service` emits `OptimizationRequestedEvent`, builds a snapshot, and runs baseline plus solver comparison.
7. `optimization-service` publishes `DriverRepositioningRecommendedEvent` and `OptimizationCompletedEvent`.
8. `simulation-service` may consume completion results later for benchmark feedback, but it does not execute the recommendations.

## Sequence Flow With Driver Location Generation Agent

1. `driver-location-generator` updates Redis GEO and emits `DriverLocationUpdatedEvent`.
2. `optimization-service` updates its latest driver snapshot cache and zone supply counters.
3. `optimization-service` publishes `DriverRepositioningRecommendedEvent`.
4. A later execution flow can translate the recommendation into `RepositionDriverCommand` for `driver-location-generator`.
5. `driver-location-generator` owns actual movement progression and emits fresh `DriverLocationUpdatedEvent` events as the driver moves.
6. `optimization-service` observes those updates and suppresses duplicate recommendations when the target zone is already in progress.

## Unit And Integration Test Plan

### Unit Tests

- `OptimizationSnapshotBuilderTest`
  - merges simulation metrics, live drivers, and pending requests into one consistent snapshot
- `DriverSnapshotReaderTest`
  - ignores offline or stale drivers from Redis-backed state
- `BaselineVsOptimizedComparatorTest`
  - reproduces nearest-five baseline and computes deterministic improvements
- `RideDispatchConstraintProviderTest`
  - rejects `ON_TRIP` or offline drivers
  - penalizes overcrowding in Wembley under `CONCERT_RAIN`
  - preserves minimum central London supply under `AIRPORT_RUSH`
- `OptimizationResultMapperTest`
  - converts Timefold solutions into persistence entities and Kafka DTOs

### Integration Tests

- PostgreSQL repository test for:
  - `optimization_run`
  - `optimization_result`
  - `baseline_vs_optimized_metrics`
  - `driver_repositioning_recommendation`
- Kafka consumer and publisher test covering:
  - `SimulationMetricsUpdatedEvent` to `OptimizationCompletedEvent`
  - `DriverRepositioningRecommendedEvent` publication
- Redis-backed snapshot test ensuring:
  - live driver coordinates are read from `vehicle_location`
  - current geo-state keys align with the emitted driver status
- solver integration test using a small London zone fixture for:
  - `CONCERT_RAIN`
  - `AIRPORT_RUSH`

### Local Verification Commands

Once implementation exists, the first validation path should be:

1. run targeted unit tests for snapshot building, constraints, and comparison logic
2. run repository integration tests against PostgreSQL
3. run Kafka plus Redis integration tests
4. run one scenario replay for `CONCERT_RAIN` and confirm recommendations keep some supply in Soho, Camden, and King’s Cross while raising Wembley coverage without saturating it

## Review Pass

No blocking findings remain in this documentation design. Residual risk is concentrated in contract alignment between `simulation-service`, Redis geo-state shape from `driver-location-generator`, and the future command handoff that turns recommendations into executed movement.
